
package comprador;

import comprador.gui.CompradorGUI;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import static jade.lang.acl.MessageTemplate.MatchPerformative;
import java.util.HashMap;
import tools.Tools;

public class Comprador extends Agent{
    
    private HashMap<String,Puja> pujasActivas;
    private CompradorGUI compradorGUI;
    
    @Override
    public void setup(){
        this.compradorGUI = new CompradorGUI(this);
        this.pujasActivas = new HashMap<>();
        
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID()); 
        ServiceDescription description = new ServiceDescription();
        description.setType("subasta-libro");
        description.setName("comprar-libro");
        agentDescription.addServices(description);

        try {
            // Registro
            DFService.register(this, agentDescription);
        } catch (FIPAException exception) {
            System.out.println(exception.getACLMessage());
            System.exit(0);
        }
        
        addBehaviour(new serInformadoSubasta());
        addBehaviour(new serInformadoGanador());
    }
    
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("[*] El agente comprador: " + getName() + " sale de la sala de subastas");
        } catch (FIPAException exception) {
            exception.printStackTrace();
            System.exit(0);
        }
    }
    
    
    public Boolean anhadirPuja(Puja puja) throws Exception{
        if(this.pujasActivas.get(puja.getLibro()) == null){
            this.pujasActivas.put(puja.getLibro(),puja);
            compradorGUI.nuevaNotificaion("Nuevo libro, precio máx: " + puja.getPrecio() + "€");
            compradorGUI.actualizarTablaPujas();
            return true;
        }
        return false;
    }
    
    public HashMap<String,Puja> getPujasActivas(){
        return this.pujasActivas;
    }
    
    public void eliminarPuja(Puja puja){
        this.pujasActivas.remove(puja.getLibro());
    }

   
    
    
    private class serInformadoSubasta extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt); 
            if (msg != null) {
                String libro = Tools.getLibroFromMessage(msg.getContent());
                Double precio = Tools.getPrecioFromMessage(msg.getContent());
                if(pujasActivas.get(libro)!=null){
                    System.out.println("Tengo ese libro!! --> " + libro);
                    Puja p = pujasActivas.get(libro);
                    if(p.getPrecio()>=precio){
                        compradorGUI.nuevaNotificaion("Subasta del libro " + libro + " --> " + precio + "€");
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setContent(msg.getContent());
                        respuesta.setPerformative(ACLMessage.PROPOSE);
                        myAgent.send(respuesta);
                    }else{
                        p.setGanador(false);
                    }
                }
                
            }
            else{
                block();
            }
        }
    }
    
    private class serInformadoGanador extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL), MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
            ACLMessage msg = myAgent.receive(mt); 
            if(msg != null){
                String libro = Tools.getLibroFromMessage(msg.getContent());
                Double precio = Tools.getPrecioFromMessage(msg.getContent());
                Puja p = pujasActivas.get(libro);
                p.setGanador(false);
                //Puja p = Tools.messageToPuja(msg.getContent());
                switch(msg.getPerformative()){
                    case ACLMessage.ACCEPT_PROPOSAL:
                        compradorGUI.nuevaNotificaion("Ganador de la ronda del libro " + libro);
                        System.out.println(getAID().getName()+ ": soy el ganador de la ronda de la subasta del libro " + libro);
                        p.setGanador(true);
                        compradorGUI.actualizarTablaPujas();
                        break;
                    case ACLMessage.REJECT_PROPOSAL:
                        compradorGUI.nuevaNotificaion("No soy el ganador de la ronda del libro " + libro);
                        System.out.println(getAID().getName()+ ": no gané la ronda de la subasta del libro " + libro);
                        p.setGanador(false);
                        compradorGUI.actualizarTablaPujas();
                        break;
                    case ACLMessage.REQUEST:
                        compradorGUI.nuevaNotificaion("Soy el ganador de la subasta del libro " + libro + " por " + precio + "€");
                        System.out.println(getAID().getName()+ ": soy el ganador de la subasta del libro " + libro + " por " + precio);
                        pujasActivas.remove(libro);
                        compradorGUI.actualizarTablaPujas();
                        compradorGUI.nuevaCompra(libro, precio);
                        break;
                }
            }
            block();            
        }
    }
}
