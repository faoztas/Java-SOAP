package com.company;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class SOAPClient {

    String SOAP_ENDPOINT;
    String SOAP_ACTION;
    String TARGET_NAMESPACE;
    String OPERATION_NAME;
    String PARAMETER_NAME;
    String PARAMETER_VALUE;

    public static void main(String args[]) {
        final String endpoint = "http://81.214.73.178/TahsilatService/TahsilatService.asmx";
        final String namespace = "http://tempuri.org/";
        final String operation = "BorcSorgu";
        final String parameter = "referansNo";
        //String value = "58302861634";
        //String value = "30295872165";

        JFrame frame = new JFrame();
        //JFrame settings

        frame.setSize(800,600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        //Table
        final DefaultTableModel model = new DefaultTableModel();
        Object[] columNames = {"BorcReferansNo","GelirID","BorcTur","DonemTaksit","SonOdemeTarih","Tutar","Gecikme","Toplam"};
        model.setColumnIdentifiers(columNames);
        JTable table = new JTable();
        table.setSize(1000,1000);
        table.setModel(model);

        //Textfield
        final JTextField textField = new JTextField(8);

        //Button
        JButton button = new JButton("Sorgula");
        button.setSize(60,30);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SOAPClient client = new SOAPClient(endpoint, namespace, operation, parameter, textField.getText());
                client.callSoapWebService(model);
            }
        });

        //Last Settings
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setSize(750,600);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(button);
        frame.add(textField);

        frame.setVisible(true);

    }

    public SOAPClient(String endpoint, String namespace, String operation, String parameter, String value) {
        SOAP_ENDPOINT = endpoint;
        SOAP_ACTION = namespace + operation;
        TARGET_NAMESPACE = namespace;
        OPERATION_NAME = operation;
        PARAMETER_NAME = parameter;
        PARAMETER_VALUE = value;
    }

    public void callSoapWebService(DefaultTableModel model) {
        try {
            // Create SOAP Connection
            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection soapConnection = soapConnectionFactory.createConnection();

            // Send SOAP Message to SOAP Server
            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), SOAP_ENDPOINT);

            // Print the SOAP Response
            System.out.println("Response SOAP Message:");
            //soapResponse.writeTo(System.out);
            //System.out.println();
            ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
            soapResponse.writeTo(outputstream);
            System.out.println(new String(outputstream.toByteArray()));

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            ByteArrayInputStream instream = new ByteArrayInputStream(outputstream.toByteArray()); //outputstream to inputstream

            Document document = documentBuilder.parse(instream);

            NodeList borcdetaylist =  document.getElementsByTagName("BorcDetay");

            Object[] rows = new Object[8];
            for(int i=1;i<borcdetaylist.getLength();i++){
                for(int j=0;j<borcdetaylist.item(i).getChildNodes().getLength();j++){
                   rows[j] = borcdetaylist.item(i).getChildNodes().item(j).getTextContent();
                }
                model.addRow(rows);
            }


            soapConnection.close();
        } catch (Exception e) {
            System.err.println("\nError occurred while sending SOAP Request to Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public SOAPMessage createSOAPRequest() throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();

        createSoapEnvelope(soapMessage);

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", SOAP_ACTION);

        soapMessage.saveChanges();

        /* Print the request message, just for debugging purposes */
        System.out.println("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println("\n");

        return soapMessage;
    }

    public void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException {
        String namespace = "namespace";
        SOAPPart soapPart = soapMessage.getSOAPPart();

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration(namespace, TARGET_NAMESPACE);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement(OPERATION_NAME, namespace);
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement(PARAMETER_NAME, namespace);
        soapBodyElem1.addTextNode(PARAMETER_VALUE);
    }
}