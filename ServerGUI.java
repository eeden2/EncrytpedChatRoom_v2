import java.net.ServerSocket;


public class ServerGUI extends javax.swing.JFrame {

    Server s;
    public ServerGUI() throws Exception {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        textArea1 = new java.awt.TextArea();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Fira Sans Condensed ExtraBold", 0, 24)); // NOI18N
        jLabel1.setText("Users");

        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "No Users Connected" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });

        jScrollPane2.setViewportView(jList1);

        textArea1.setEditable(false);

        textArea1.addTextListener(new java.awt.event.TextListener() {
            public void textValueChanged(java.awt.event.TextEvent evt) {
                textArea1TextValueChanged(evt);
            }
        });

        jButton2.setText("Close the Server");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try{
                    for(ClientHandler ch : s.linker.keySet()){
                        ch.closer(ch.s,ch.br,ch.bw);
                    }
                    s.server.close();
                    textArea1.setText("");
                    System.exit(1);
                }catch(Exception e){System.out.println("Button didnt work.");}


            }
        });
        jButton1.setText("Start the Server");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(54, 54, 54)
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(20, 20, 20)
                                                .addComponent(textArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 588, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane2))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(textArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                                                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>

    public void userListChanger(String[] users)
    {
        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] string = users;
            public int getSize() { return string.length; }
            public String getElementAt(int i) { return string[i]; }
        });
    }
    private void textArea1TextValueChanged(java.awt.event.TextEvent evt) {

    }


    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        try{
            s = new Server(new ServerSocket(5000));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    s.startServer();
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        String temp = s.getcurrentMessage();
                        textArea1.append(temp+"\n");
                        while(!s.server.isClosed()){
                            String temp2 = s.getcurrentMessage();
                            if(temp!=temp2){
                                textArea1.append(temp2+"\n");
                                temp = temp2;
                            }
                        }
                    }catch(Exception e){}

                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] strings;
                    strings = s.getUsers();
                    int size = strings.length;
                    while(s.serverStatus())
                    {
                        strings = s.getUsers();
                        if(size == 0) userListChanger(new String[]{"No Users Connected"});
                        if(strings.length>0){
                            if(strings[0].equals("No Users Connected"))
                        {
                            size = 0;
                        }
                        }
                        
                        if(strings.length!=size)
                        {
                            userListChanger(strings);
                            size = strings.length;   
                        }
                        

                    }
                    userListChanger(new String[]{""});
               }
            }).start();
        }catch(Exception e){}
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run(){
                try{
                    new ServerGUI().setVisible(true);
                }catch(Exception e){}
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane2;
    private java.awt.TextArea textArea1;
    // End of variables declaration
}