
import javax.crypto.Cipher;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class clientGUI extends javax.swing.JFrame {

    // initialize socket and input output
    public Socket               socket  = null;
    public BufferedReader input   = null;
    public BufferedWriter out     = null;
    public String user;
    private PublicKey publicKey;

    public String currentMessage = "No Messages Yet";
    private PrivateKey privateKey;
    private PublicKey serverPubKey;
    public clientGUI(Socket s, String str) throws Exception {
        socket = s;
        user = str;
        //Client Initiation
        // establish a connection
        try
        {
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            

            // Initialise RSA
            try{
                KeyPairGenerator RSAKeyGen = KeyPairGenerator.getInstance("RSA");
                RSAKeyGen.initialize(2048);
                KeyPair pair = RSAKeyGen.generateKeyPair();
                this.publicKey = pair.getPublic();
                this.privateKey = pair.getPrivate();
            } catch (GeneralSecurityException e) {
                System.out.println(e.getLocalizedMessage() + "\n");
                System.out.println("Error initialising encryption. Exiting.\n");
                System.exit(1);
            }
            //Sending Username to Server
            out.write(str);
            out.newLine();
            out.flush();
            //SEnding PublicKey to Server
            try{
                ByteBuffer bb = ByteBuffer.allocate(4);
                bb.putInt(publicKey.getEncoded().length);
                socket.getOutputStream().write(bb.array());
                socket.getOutputStream().write(publicKey.getEncoded());
                socket.getOutputStream().flush();
            }catch(IOException e){
                System.out.println("IO Problem");
            }


            //Read-In the Server Public Key
            try{
                byte[] l = new byte[4];
                socket.getInputStream().read(l,0,4);
                ByteBuffer bb = ByteBuffer.wrap(l);
                int len = bb.getInt();
                byte[] pubKeyByte = new byte[len];
                socket.getInputStream().read(pubKeyByte);


                X509EncodedKeySpec kys = new X509EncodedKeySpec(pubKeyByte);
                KeyFactory kfs = KeyFactory.getInstance("RSA");
                serverPubKey = kfs.generatePublic(kys);

            }catch(Exception e){
                e.printStackTrace();
            }

            initComponents();
            listener();

        }
        catch(IOException i)
        {
            closer(socket,input,out);
        }
        
    }


    private void initComponents() {

        ChatDisplay = new java.awt.TextArea();
        Writer = new javax.swing.JTextField();
        Sender = new javax.swing.JButton();
        Disconnect = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();


        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        ChatDisplay.setEditable(false);
        ChatDisplay.addTextListener(new java.awt.event.TextListener() {
            public void textValueChanged(java.awt.event.TextEvent evt) {
            }
        });

        Sender.setText("Send");
        Sender.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SenderMouseClicked(evt);
            }
        });

        Disconnect.setText("Disconnect");
        Disconnect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                DisconnectMouseClicked(evt);
            }
        });
        Disconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DisconnectActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Fira Sans Book", 0, 24)); // NOI18N
        jLabel4.setText("Chatroom");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(Writer)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(Sender, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addContainerGap())
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(ChatDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 644, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(Disconnect)
                                                        .addComponent(jLabel4))
                                                .addGap(0, 6, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(ChatDisplay, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 327, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addGap(16, 16, 16)
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(Disconnect, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(Writer, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                        .addComponent(Sender, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        pack();
    }


    private void DisconnectActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void DisconnectMouseClicked(java.awt.event.MouseEvent evt) {
      try{
          socket.close();
          System.exit(1);
      }catch(Exception e){}

    }

    public void closer(Socket s, BufferedReader br, BufferedWriter out)
    {
        try
        {
            s.close();
            br.close();
            out.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void listener()
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String temp = input.readLine();
                    ChatDisplay.append(decrypter(temp)+"\n");
                    while(socket.isConnected()){
                        //ChatDisplay.setText("In the loop in listener.");
                        String temp2 = input.readLine();
                        if(temp!=temp2){
                            ChatDisplay.append(decrypter(temp2)+"\n");
                            temp = temp2;
                        }
                    }
                }catch(Exception e){}

            }
        }).start();
    }

    public void setCurrentMessage(String s) {currentMessage = s;}
    public String getcurrentMessage(){return currentMessage;}

    public String decrypter(String message) throws Exception
    {
        Base64.Decoder decoder = Base64.getDecoder();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        try{
            return new String(cipher.doFinal(decoder.decode(message)));
        }catch(Exception e){return "Message Failed to Decrypt";}

    }

    public byte[] encrypter(String message) throws Exception
    {
        Cipher encrypter = Cipher.getInstance("RSA");
        encrypter.init(Cipher.ENCRYPT_MODE,serverPubKey);
        byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encrypter.doFinal(secretMessageBytes);
        return encryptedMessageBytes;
    }

    private void SenderMouseClicked(java.awt.event.MouseEvent evt) {
        try{
            String message = Writer.getText();
            Base64.Encoder base64 = Base64.getEncoder();
            String encryptedString = base64.encodeToString(encrypter(user + ": "+message));
            out.write(encryptedString);
            out.newLine();
            out.flush();
        }catch(Exception e){}

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws Exception{
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(clientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(clientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(clientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(clientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Create frame with title Registration Demo
                JFrame frame= new JFrame();
                frame.setTitle("The Bridge");
                JPanel mainPanel = new JPanel();
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                JPanel headingPanel = new JPanel();
                JLabel headingLabel = new JLabel("Connect to a Chatroom");
                headingPanel.add(headingLabel);
                JPanel panel = new JPanel(new GridBagLayout());
// Constraints for the layout
                GridBagConstraints constr = new GridBagConstraints();
                constr.insets = new Insets(5, 5, 5, 5);
                constr.anchor = GridBagConstraints.WEST;
// Setting initial grid values to 0,0
                constr.gridx=0;
                constr.gridy=0;
                JLabel ipLabel      = new JLabel("Enter an IP :");
                JLabel portLabel      = new JLabel("Enter a Port :");
                JLabel usernameLabel     = new JLabel("Enter your Username :");
                JTextField ipInput           = new JTextField(20);
                JTextField portInput           = new JTextField(20);
                JTextField usernameInput          = new JTextField(20);
                panel.add(ipLabel, constr);
                constr.gridx=1;
                panel.add(ipInput, constr);
                constr.gridx=0; constr.gridy=1;
                panel.add(portLabel, constr);
                constr.gridx=1;
                panel.add(portInput, constr);
                constr.gridx=0; constr.gridy=2;
                panel.add(usernameLabel, constr);
                constr.gridx=1;
                panel.add(usernameInput, constr);
                constr.gridx=0; constr.gridy=3;
                constr.anchor = GridBagConstraints.CENTER;

// Button with text "Register"
                JButton button = new JButton("Connect");
// add a listener to button
                button.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        try{
                            frame.dispose();
                            new clientGUI(new Socket(ipInput.getText(), Integer.parseInt(portInput.getText())), usernameInput.getText()).setVisible(true);
                        }catch(Exception i){}

                    }
                });
                panel.add(button, constr);
                mainPanel.add(headingPanel);
                mainPanel.add(panel);
                frame.add(mainPanel);
                frame.pack();
                frame.setSize(500, 500);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private java.awt.TextArea ChatDisplay;
    private javax.swing.JButton Disconnect;
    private javax.swing.JButton Sender;
    private javax.swing.JTextField Writer;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration
}
