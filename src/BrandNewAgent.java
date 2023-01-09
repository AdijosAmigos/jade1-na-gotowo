package jadelab1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.net.*;
import java.io.*;




public class BrandNewAgent extends Agent {
  protected void setup () {
    //services registration at DF
    DFAgentDescription dfad = new DFAgentDescription();
    dfad.setName(getAID());
    //service no 1
    ServiceDescription sd1 = new ServiceDescription();
    sd1.setType("answers");
    sd1.setName("wordnet");
    //service no 2
    ServiceDescription sd2 = new ServiceDescription();
    sd2.setType("answers");
    sd2.setName("polhol");
    //add them all
    dfad.addServices(sd1);
    dfad.addServices(sd2);
    try {
      DFService.register(this,dfad);
    } catch (FIPAException ex) {
      ex.printStackTrace();
    }

    addBehaviour(new WordnetCyclicBehaviour2(this));
    addBehaviour(new DictOrg_DictionaryCyclicBehaviour2(this));
    //doDelete();
  }

  protected void takeDown() {
    //services deregistration before termination
    try {
      DFService.deregister(this);
    } catch (FIPAException ex) {
      ex.printStackTrace();
    }
  }

  public String makeRequest(String serviceName, String word)
  {
    StringBuffer response = new StringBuffer();
    try {
      URL url;
      URLConnection urlConn;
      DataOutputStream printout;
      DataInputStream input;
      url = new URL("http://dict.org/bin/Dict");
      urlConn = url.openConnection();
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);
      urlConn.setUseCaches(false);
      urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      String content = "Form=Dict1&Strategy=*&Database=" + URLEncoder.encode(serviceName) + "&Query=" + URLEncoder.encode(word) + "&submit=Submit+query";
      //forth
      printout = new DataOutputStream(urlConn.getOutputStream());
      printout.writeBytes(content);
      printout.flush();
      printout.close();
      //back
      input = new DataInputStream(urlConn.getInputStream());
      String str;
      while (null != ((str = input.readLine()))) {
        response.append(str);
      }
      input.close();
    }
    catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
    //cut what is unnecessary
    return response.substring(response.indexOf("<hr>")+4, response.lastIndexOf("<hr>"));
  }
}

class WordnetCyclicBehaviour2 extends CyclicBehaviour {

  BrandNewAgent agent;

  public WordnetCyclicBehaviour2(BrandNewAgent agent)
  {
    this.agent = agent;
  }

  public void action(){
    MessageTemplate template = MessageTemplate.MatchOntology("wordnet");
    ACLMessage message = agent.receive(template);
    if (message == null) {
      block();
    }
    else{
      //process the incoming message
      String content = message.getContent();
      ACLMessage reply = message.createReply();
      reply.setPerformative(ACLMessage.INFORM);
      String response = "";
      try {
        response = agent.makeRequest("wn",content);
      }
      catch (NumberFormatException ex) {
        response = ex.getMessage();
      }
      reply.setContent(response);
      agent.send(reply);
    }
  }
}

class DictOrg_DictionaryCyclicBehaviour2 extends CyclicBehaviour {

  BrandNewAgent agent;

  public DictOrg_DictionaryCyclicBehaviour2(BrandNewAgent agent) {
    this.agent = agent;
  }

  public void action(){
    MessageTemplate template = MessageTemplate.MatchOntology("polhol");
    ACLMessage message = agent.receive(template);
    if (message == null){
      block();
    }
    else{
      //process the incoming message
      String content = message.getContent();
      ACLMessage reply = message.createReply();
      reply.setPerformative(ACLMessage.INFORM);
      String response = "";
      try {
        response = agent.makeRequest("fd-pol-nld", content);
      }
      catch (NumberFormatException ex) {
        response = ex.getMessage();
      }
      reply.setContent(response);
      agent.send(reply);
    }
  }
}