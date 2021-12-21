
package vendedor;

import comprador.Puja;
import vendedor.gui.VendedorGUI;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import tools.Definiciones;
import tools.Tools;

public class Vendedor extends Agent{
    protected static HashMap<String,HashMap<Integer,Subasta>> subastasActivas = new HashMap<>();
    protected static AID[] posiblesCompradores;
    //protected static ArrayList<Subasta> subastasPendientes = new ArrayList<>();
    private VendedorGUI vendedorGUI;
    protected Boolean flag = true;
   
    
   
    @Override
    public void setup(){
        
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID()); 
        ServiceDescription description = new ServiceDescription();
        description.setType("subasta-libro");
        description.setName("vender-libro");
        agentDescription.addServices(description);

        try {
            // Registro
            DFService.register(this, agentDescription);
        } catch (FIPAException exception) {
            System.out.println(exception.getACLMessage());
            System.exit(0);
        }
        
        
        System.out.println("Hola Mundo, soy el servidor!");
        this.vendedorGUI = new VendedorGUI(this);
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                if(flag){
                    flag = false;
                    System.out.println("------------------ Subastador -------------------");
                    addBehaviour(new Subastar());
                }
            }
        });
        
    }
    
    public HashMap<String,HashMap<Integer,Subasta>> getSubastasActivas(){
        return this.subastasActivas;
    }
    
    public void anhadirLibro(String titulo, Double precio, Double incremento) throws Exception{
        
        Subasta s = new Subasta(titulo,precio,incremento);
        
        if(!subastasActivas.containsKey(titulo)){
            subastasActivas.put(titulo, new HashMap<>());
        }
        subastasActivas.get(titulo).put(s.getId(),s);
        
        this.vendedorGUI.actualizarTablaSubastas();
        
        this.vendedorGUI.nuevaNotificacion("Subasta del libro: [" + s.getId()+"] " + titulo);
        
    }
    
    public class Subastar extends Behaviour{
        
        private long endTime;
        private int step;
        
        @Override 
        public void onStart(){
            step = 0;
            endTime = System.currentTimeMillis() + Definiciones.TIMEOUT;
        }
        
        @Override
        public void action() {
            
            switch(step){
                case 0:
                    
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("subasta-libro");
                    sd.setName("comprar-libro");
                    template.addServices(sd);
                    try {

                        DFAgentDescription[] result = DFService.search(myAgent, template); 
                        posiblesCompradores = new AID[result.length];

                        for (int i = 0; i < result.length; ++i) {
                            System.out.println("COMPRADOR: " + result[i].getName().getName());
                            posiblesCompradores[i] = result[i].getName();
                        }

                    }
                    catch (FIPAException fe) {
                            fe.printStackTrace();
                    }
                    System.out.println("LOS LIBROS----------");
                    for(HashMap<Integer,Subasta> HS: subastasActivas.values()){
                        for(Subasta s: HS.values()){
                            if(s.getFinalizada()==false){
                                System.out.println("Se subasta el libro: " + s.getLibro());
                                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                                msg.setContent(Tools.subastaToMessage(s));
                                for(AID comprador: posiblesCompradores){
                                    System.out.println("CFP enviado a " + comprador.getName() + " ---> " + s.getLibro());
                                    msg.addReceiver(comprador);
                                }
                                myAgent.send(msg);
                            }
                        }
                    }
                    
                    step = 1;
                    
                    break;
                case 1:
                    
                    if(endTime > System.currentTimeMillis()){
                        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                        ACLMessage m = myAgent.receive(mt); 
                        if(m != null){
                            Integer idSubasta = Tools.getIdFromMessage(m.getContent());
                            String titulo = Tools.getLibroFromMessage(m.getContent());
                            ACLMessage msgReply = m.createReply();
                            msgReply.setContent(m.getContent());
                            
                            Subasta s = subastasActivas.get(titulo).get(idSubasta);
                            
                            if(s.getParticipantes().isEmpty()){
                                s.setGanador(null);
                            }
                            
                            s.anhadirParticante(m.getSender());
                            
                            if(s.getGanador() == null && posibleGanador(s,m.getSender())){
                                s.setGanador(m.getSender());
                                msgReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                myAgent.send(msgReply);
                                vendedorGUI.nuevaNotificacion("[" + s.getId()+"] -> W ronda: "+s.getGanador().getLocalName());
                            }else{
                                msgReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                myAgent.send(msgReply);
                            }
                            vendedorGUI.actualizarTablaSubastas();
                        }
                        
                    }else{
                        step = 2; 
                    }
                    
                    break;
                case 2:
                    
                    for(HashMap<Integer,Subasta> HS: subastasActivas.values()){
                        for(Subasta s: HS.values()){
                            if(s.getGanador() != null && s.getFinalizada()==false){
                                if(s.getParticipantes().size() > 1){
                                    s.setPrecioAnterior(s.getPrecio());
                                    s.setPrecio(s.getPrecio()+s.getIncremento());
                                    s.getParticipantes().clear();
                                    vendedorGUI.nuevaNotificacion("[" + s.getId()+"] -> Precio sube: " + s.getPrecio() + "€");
                                }else if(s.getParticipantes().size() == 1){
                                    ACLMessage mm = new ACLMessage(ACLMessage.REQUEST);
                                    mm.setContent(Tools.subastaToMessage(s));
                                    mm.addReceiver(s.getGanador());
                                    myAgent.send(mm);
                                    subastasActivas.get(s.getLibro()).remove(s.getId());
                                    vendedorGUI.nuevaNotificacion("[" + s.getId()+"] -> W subasta: "+s.getGanador().getLocalName() + " -- " + s.getPrecio() + "€");
                                    s.setFinalizada(true);
                                }else if(s.getParticipantes().isEmpty()){
                                    s.setPrecio(s.getPrecioAnterior());
                                    ACLMessage mm = new ACLMessage(ACLMessage.REQUEST);
                                    mm.setContent(Tools.subastaToMessage(s));
                                    mm.addReceiver(s.getGanador());
                                    myAgent.send(mm);
                                    s.setFinalizada(true);
                                    vendedorGUI.nuevaNotificacion("[" + s.getId()+"] -> W subasta: "+s.getGanador().getLocalName() + " -- " + s.getPrecio() + "€");
                                }
                            }
                        }
                    }
                    vendedorGUI.actualizarTablaSubastas();
                    
                    step = 3;
                    
                    break;
            }
            
        }

        @Override
        public boolean done() {
            return step == 3;
        }
        
        @Override
        public int onEnd(){
            flag = true;
            return 0;
        }
        
        private Boolean posibleGanador(Subasta subasta, AID comprador){
            for(Subasta s: subastasActivas.get(subasta.getLibro()).values()){
                if(s.getGanador() != null && s.getGanador().equals(comprador) && s.getFinalizada()==false){
                    return false;
                }
            }
            return true;
        }
        
    }
    
    
    
    
    /*
    public class Subastar extends Behaviour{
        
        private Subasta subasta;
        
        protected AID[] posiblesCompradores;
        
        private Integer step;
        
        private long timeLimit;
        
        public Subastar(Agent a, Subasta s){
            super(a);
            this.subasta = s;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
        }
        
        @Override
        public void onStart(){
            timeLimit = System.currentTimeMillis() + Definiciones.TIMEOUT;
            step = 0;
        }

        @Override
        public void action() {
            switch(step){
                case 0:
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("subasta-libro");
                    template.addServices(sd);
                    try {

                        DFAgentDescription[] result = DFService.search(myAgent, template); 
                        posiblesCompradores = new AID[result.length];

                        for (int i = 0; i < result.length; ++i) {
                            System.out.println("COMPRADOR: " + result[i].getName().getName());
                            posiblesCompradores[i] = result[i].getName();
                        }

                    }
                    catch (FIPAException fe) {
                            fe.printStackTrace();
                    }
                    
                    System.out.println("Se subasta el libro: " + subasta.getLibro());
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    msg.setContent(Tools.subastaToMessage(subasta));
                    for(AID comprador: posiblesCompradores){
                        System.out.println("CFP enviado a " + comprador.getName() + " ---> " + subasta.getLibro());
                        msg.addReceiver(comprador);
                    }
                    myAgent.send(msg);
                    
                    step = 1;
                    break;
                case 1:
                    if(timeLimit <= System.currentTimeMillis()){

                        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                        ACLMessage m = myAgent.receive(mt); 
                        if(m != null){
                            Puja puja = Tools.messageToPuja(m.getContent());
                            ACLMessage msgReply = m.createReply();
                            msgReply.setContent(m.getContent());
                            if(subasta.getParticipantes().isEmpty()){
                                subasta.setGanador(m.getSender());
                                msgReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                myAgent.send(msgReply);
                            }else{
                                msgReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                myAgent.send(msgReply);
                            }

                            subasta.anhadirParticante(m.getSender());
                        }else{
                            //block();
                        }
                    }else{
                        if(subasta.getParticipantes().isEmpty() && subasta.getGanador() == null){
                            step = 0;
                        }else{
                            step = 2;
                        }
                        
                    }
                    break;
                case 2: 
                    if(subasta.getParticipantes().size() > 1){
                        subasta.setPrecioAnterior(subasta.getPrecio());
                        subasta.setPrecio(subasta.getPrecio()+subasta.getIncremento());
                        System.out.println("Nuevo precio del libro: " + subasta.getLibro() + " --> " + subasta.getPrecio());
                        step = 0;
                    }else if(subasta.getParticipantes().isEmpty()){
                       subasta.setPrecio(subasta.getPrecioAnterior());
                       ACLMessage mm = new ACLMessage(ACLMessage.REQUEST);
                       mm.setContent(Tools.subastaToMessage(subasta));
                       mm.addReceiver(subasta.getGanador());
                       myAgent.send(mm);
                       //subastasActivas.remove(subasta); // --> revisar los ids y así
                       step = 3;
                    }else if(subasta.getParticipantes().size() == 1){
                       ACLMessage mm = new ACLMessage(ACLMessage.REQUEST);
                       mm.setContent(Tools.subastaToMessage(subasta));
                       mm.addReceiver(subasta.getGanador());
                       myAgent.send(mm);
                       //subastasActivas.remove(subasta);// --> revisar los ids y así    
                       step = 3;
                    }

                    subasta.getParticipantes().clear();
                    
                    break;
                case 3:
                
                    step = 4;
                        
                    break;
            }
        }

        @Override
        public boolean done() {
            return step == 4;
        }
    }
    */
    
    
    
    /*
    public class Subastar extends Behaviour{
        
        private Integer step = 0;
        private long endTime;
        
        @Override 
        public void onStart(){
            System.out.println("ARRANCA EL SUBASTAR BEHAVIOUR");
            step = 0;
            endTime = System.currentTimeMillis() + Definiciones.TIMEOUT;
        }
        
        @Override
        public void action() {
            switch(step){
                case 0:
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("subasta-libro");
                    template.addServices(sd);
                    try {

                        DFAgentDescription[] result = DFService.search(myAgent, template); 
                        posiblesCompradores = new AID[result.length];

                        for (int i = 0; i < result.length; ++i) {
                            System.out.println("COMPRADOR: " + result[i].getName().getName());
                            posiblesCompradores[i] = result[i].getName();
                        }

                    }
                    catch (FIPAException fe) {
                            fe.printStackTrace();
                    }
                    
                    System.out.println("LOS LIBROS----------");
                    // Aviso a todos los posibles compradores (Envío CFP)
                    for(String libroSubasta: subastasActivas.keySet()){
                        System.out.println("Se subasta el libro: " + libroSubasta);
                        Subasta s = subastasActivas.get(libroSubasta);
                        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                        msg.setContent(Tools.subastaToMessage(s));
                        for(AID comprador: posiblesCompradores){
                            System.out.println("CFP enviado a " + comprador.getName() + " ---> " + libroSubasta);
                            msg.addReceiver(comprador);
                        }
                        myAgent.send(msg);
                    }
                    
                    step = 1;

                    break;
                case 1:
                    if(endTime > System.currentTimeMillis()){
                        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                        ACLMessage msg = myAgent.receive(mt); 
                        if(msg != null){
                            Puja puja = Tools.messageToPuja(msg.getContent());
                            Subasta s = subastasActivas.get(puja.getLibro());
                            ACLMessage msgReply = msg.createReply();
                            msgReply.setContent(msg.getContent());
                            if(s.getParticipantes().isEmpty()){
                                s.setGanador(msg.getSender());
                                msgReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                myAgent.send(msgReply);
                            }else{
                                msgReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                myAgent.send(msgReply);
                            }
                            
                            s.anhadirParticante(msg.getSender());
                            
                        }else{
                            //block();
                        }
                    }else{
                        step = 2;
                    }                                      
                    break;
                case 2:
                    
                    for(String libro: subastasActivas.keySet()){
                        Subasta s = subastasActivas.get(libro);
                        if(s.getParticipantes().size() > 1){
                            s.setPrecioAnterior(s.getPrecio());
                            s.setPrecio(s.getPrecio()+s.getIncremento());
                            System.out.println("Nuevo precio del libro: " + libro + " --> " + s.getPrecio());
                        }else if(s.getParticipantes().isEmpty()){
                           s.setPrecio(s.getPrecioAnterior());
                           ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                           msg.setContent(Tools.subastaToMessage(s));
                           msg.addReceiver(s.getGanador());
                           myAgent.send(msg);
                           subastasActivas.remove(libro);
                        }else if(s.getParticipantes().size() == 1){
                           ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                           msg.setContent(Tools.subastaToMessage(s));
                           msg.addReceiver(s.getGanador());
                           myAgent.send(msg);
                           subastasActivas.remove(libro);
                        }
                        
                        s.getParticipantes().clear();
                    }
                    
                    step = 3;
                    
                    break;
                case 3:
                    
                    step = 4;
                    
                    break;
            }
        
        }

        @Override
        public boolean done() {
            return step == 4; 
        }
        
        @Override
        public int onEnd(){
            flag = true;
            return 0;
        }
        
    }*/
    
    /*
    public class Avisar extends OneShotBehaviour{
        
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("subasta-libro");
            template.addServices(sd);
            try {
                
                DFAgentDescription[] result = DFService.search(myAgent, template); 
                posiblesCompradores = new AID[result.length];
                
                for (int i = 0; i < result.length; ++i) {
                    System.out.println("COMPRADOR: " + result[i].getName());
                    posiblesCompradores[i] = result[i].getName();
                }

            }
            catch (FIPAException fe) {
                    fe.printStackTrace();
            }
            
            // Aviso a todos los posibles compradores (Envío CFP)
            for(String libroSubasta: subastasActivas.keySet()){
                Subasta s = subastasActivas.get(libroSubasta);
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setContent(Tools.subastaToMessage(s));
                for(AID comprador: posiblesCompradores){
                    System.out.println("CFP enviado a " + comprador.getName() + " ---> " + libroSubasta);
                    msg.addReceiver(comprador);
                }
                myAgent.send(msg);
                
                addBehaviour(new GestionarPujas(myAgent,5000));
            }
        }
        
    }
    
    
    public class GestionarPujas extends Behaviour{
        
        private long timeout; 
        private long wakeupTime;
        private boolean finished = false;

        public GestionarPujas(Agent a, long timeout){
            super(a);
            this.timeout = timeout;
        }
        
        @Override
        public void onStart() {
            wakeupTime = System.currentTimeMillis() + timeout;
        }

        @Override
        public void action(){
            System.out.println("eoooo");
            long dt = wakeupTime - System.currentTimeMillis();
            System.out.println("DT: " + dt);
            if (dt <= 0) {
                System.out.println("finnn action");
                checkResultados();
                finished = true;
               //checkResultados();
            } else{
                System.out.println("ey0");
                // me llegan mensajes y los voy a responder con un accept proposal o con un reject proposal
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                ACLMessage msg = myAgent.receive(mt); 
                if(msg != null){
                    Puja p = Tools.messageToPuja(msg.getContent());
                    System.out.println("CONFIRMACION DE " + msg.getSender().getName() + " ---> " + p.getLibro()); 
                    Subasta s = subastasActivas.get(p.getLibro());
                    ACLMessage msgReply = msg.createReply();
                    msgReply.setContent(msg.getContent());
                    if(s.getParticipantes().isEmpty()){
                        s.setGanador(msg.getSender());
                        msgReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    }else{
                        msgReply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    }
                    s.anhadirParticante(msg.getSender());
                    myAgent.send(msgReply);
                }else{
                    System.out.println("block");
                    block();
                    System.out.println("unlock");
                }                
            }
         } 

        public boolean done() { 
            if(finished) System.out.println("jfklsjsl");
            return finished; 
        }
        
        public void checkResultados(){
            System.out.println("llegó al check");
            for(String s: subastasActivas.keySet()){
                System.out.println("En el check: " + " ---> " + s);
                Subasta subasta = subastasActivas.get(s); 
                System.out.println("EN LIBRO: " + subasta.getLibro() + " tiene " + subasta.getParticipantes().size() + " participantes");
                if(subasta.getParticipantes().isEmpty()){
                    subasta.setPrecio(subasta.getPrecioAnterior());
                    AID ganador = subasta.getGanador();
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.setContent(Tools.subastaToMessage(subasta));
                    msg.addReceiver(ganador);
                    subastasActivas.remove(s);
                    myAgent.send(msg);
                }else if(subasta.getParticipantes().size()==1){
                    AID ganador = subasta.getGanador();
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.setContent(Tools.subastaToMessage(subasta));
                    msg.addReceiver(ganador);
                    subastasActivas.remove(s);
                    myAgent.send(msg);
                }else{
                    subasta.setPrecioAnterior(subasta.getPrecio());
                    subasta.setPrecio(subasta.getPrecio()+subasta.getIncremento());
                    System.out.println("EL LIBRO: " + subasta.getLibro() + " aumento el precio a " + subasta.getPrecio());
                }
            }
            System.out.println("acabó el check");
            flag = true;

            addBehaviour(new Avisar());
        }
    }
*/  
}
