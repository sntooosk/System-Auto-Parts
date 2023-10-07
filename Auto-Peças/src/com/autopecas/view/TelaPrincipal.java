/*
 * The MIT License
 *
 * Copyright 2023 Juliano cassimiro dos Santos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.autopecas.view;

import com.autopecas.factory.ModuloConexao;
import com.autopecas.model.FormataMoeda;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * Tela JFrame para a aplicação de ponto de venda. Esta classe representa a tela
 * principal onde os produtos são registrados.
 *
 * @author Juliano
 * @version 1.10
 */
public class TelaPrincipal extends javax.swing.JFrame {

    private final FormataMoeda conv = new FormataMoeda();
    private final Connection conexao;
    private PreparedStatement pst;
    private ResultSet rs;
    private float subtotal;
    private final String[] itemArray = new String[5];

    private final Date date = new Date();

    private float preco;
    private float quant;

    /**
     * Construtor da classe TelaPrincipal. Inicializa a interface gráfica e
     * estabelece a conexão com o banco de dados.
     */
    public TelaPrincipal() {
        initComponents();
        /**
         * Método responsável pela conexao do Banco
         */
        conexao = ModuloConexao.conectar();
        /**
         * Método responsável pra Atualizar Tabela
         */
        atualizaTabela(itemArray);

    }

    /**
     * Método para buscar um item no banco de dados e preencher os campos na
     * tela.
     *
     * @param sql A consulta SQL para buscar o item.
     * @param textField O campo de texto que contém o código do item.
     */
    private void buscaItem(String sql, JTextField textField) {
        try {
            pst = conexao.prepareStatement(sql);
            pst.setString(1, textField.getText());
            rs = pst.executeQuery();

            if (rs.next()) {
                String codProd = rs.getString("tb01_cod_prod");
                preco = Float.parseFloat(rs.getString("tb01_preco"));
                quant = Float.parseFloat(cmbQuantItem.getSelectedItem().toString());

                DefaultTableModel tm = (DefaultTableModel) tbItem.getModel();

                for (int i = 0; i < tm.getRowCount(); i++) {
                    if (codProd.equals(String.valueOf(tm.getValueAt(i, 0)))) {
                        float qtdAntiga = Float.parseFloat((String) tm.getValueAt(i, 2)) + quant;
                        float valAtual = preco * qtdAntiga;
                        tm.setValueAt(String.valueOf(qtdAntiga), i, 2);
                        tm.setValueAt(conv.ConvMoeda(String.valueOf(valAtual)), i, 4);
                        calcularSubtotal();
                        return;
                    }
                }

                itemArray[0] = codProd;
                itemArray[1] = rs.getString("tb01_Descricao");
                itemArray[2] = cmbQuantItem.getSelectedItem().toString();
                itemArray[3] = conv.ConvMoeda(String.valueOf(preco));
                itemArray[4] = conv.ConvMoeda(String.valueOf(quant * preco));

                tm.addRow(itemArray);
                calcularSubtotal();
            }

            Image img = Toolkit.getDefaultToolkit().createImage(rs.getBytes("tb01_foto"));
            ImageIcon foto = new ImageIcon(img.getScaledInstance(lblFotoItem.getWidth(), lblFotoItem.getHeight(), Image.SCALE_DEFAULT));
            lblFotoItem.setIcon(foto);
            txtCodItem.requestFocus();

        } catch (SQLException e) {
            // Lida com exceções de SQL aqui
        }
    }

    /**
     * Calcula e atualiza o subtotal com base nas linhas da tabela.
     */
    private void calcularSubtotal() {
        subtotal += quant * preco;
        txtSubtotalVenda.setText(conv.ConvMoeda(String.valueOf(subtotal)));
        txtValorItem.setText(conv.ConvMoeda(String.valueOf(preco)));
    }

    /**
     * Método para adicionar um item à tabela na tela.
     *
     * @param itemArray Um array com os detalhes do item.
     */
    private void atualizaTabela(String[] itemArray) {
        DefaultTableModel tabela = (DefaultTableModel) tbItem.getModel();
        tabela.addRow(itemArray);
        tbItem.setModel(tabela);
    }

    /**
     * Método para cancelar um item da tabela na tela.
     */
    private void cancelarItem() {
        DefaultTableModel tabela = (DefaultTableModel) tbItem.getModel();
        int linha = tbItem.getSelectedRow();
        if (linha >= 0) {
            tabela.removeRow(linha);
            subtotal -= preco * quant;
            txtSubtotalVenda.setText(conv.ConvMoeda(String.valueOf(subtotal)));
            txtValorItem.setText(conv.ConvMoeda(String.valueOf(0)));
            lblFotoItem.setIcon(null);
        }
    }

    /**
     * Método para finalizar a venda, limpando a tela e atualizando o banco de
     * dados.
     */
    private void finalizarVenda() {
        DefaultTableModel tabela = (DefaultTableModel) tbItem.getModel();
        if (tabela.getRowCount() > 0) {
            lblFotoItem.setIcon(null);
            txtValorItem.setText(conv.ConvMoeda(String.valueOf(0)));
            txtSubtotalVenda.setText(conv.ConvMoeda(String.valueOf(0)));
            txtCodItem.setText("");
            txtDescricaoItem.setText("");
            subtotal = 0;
            atualizaItem();
            tabela.setRowCount(0);
        } else {
            JOptionPane.showMessageDialog(null, "Você não pode finalizar a venda porque a tabela está vazia.");
        }
    }

    /**
     * Método para atualizar o estoque do item no banco de dados após a venda.
     */
    private void atualizaItem() {
        String sql = "UPDATE tb01_produtos SET tb01_qtde = tb01_qtde - ? WHERE tb01_cod_prod = ?";

        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            int quantidade = Integer.parseInt(cmbQuantItem.getSelectedItem().toString());
            pst.setInt(1, quantidade);
            pst.setString(2, txtCodItem.getText());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lbData = new javax.swing.JLabel();
        lbHora = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCodItem = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtDescricaoItem = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbItem = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btnF1 = new javax.swing.JLabel();
        btnF6 = new javax.swing.JLabel();
        btnF12 = new javax.swing.JLabel();
        txtVendas = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblFotoItem = new javax.swing.JLabel();
        txtValorItem = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtSubtotalVenda = new javax.swing.JLabel();
        cmbQuantItem = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        lblPesquisarCod = new javax.swing.JLabel();
        lblPesquisarDescricao = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("X - Gestao");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel2.setBackground(new java.awt.Color(0, 51, 255));

        lblLogo.setBackground(new java.awt.Color(255, 255, 255));
        lblLogo.setFont(new java.awt.Font("Arial", 0, 30)); // NOI18N
        lblLogo.setForeground(new java.awt.Color(255, 255, 255));

        lbData.setFont(new java.awt.Font("Arial", 1, 17)); // NOI18N
        lbData.setForeground(new java.awt.Color(255, 255, 255));

        lbHora.setFont(new java.awt.Font("Arial", 1, 17)); // NOI18N
        lbHora.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 594, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbData, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                    .addComponent(lbHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(41, 41, 41))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbData, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbHora, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        jLabel2.setText("Consultar Item por codigo:");

        txtCodItem.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        jLabel6.setText("Consultar Item por Descriçao:");

        txtDescricaoItem.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N

        tbItem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Codigo", "Descriçao", "Quantidade", " V.Unit", "SubTotal"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tbItem);

        jPanel1.setBackground(new java.awt.Color(255, 153, 51));

        btnF1.setFont(new java.awt.Font("Arial", 1, 15)); // NOI18N
        btnF1.setText("  F1 - Finalizar Venda");
        btnF1.setToolTipText("");
        btnF1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        btnF1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnF1MouseClicked(evt);
            }
        });
        btnF1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnF1KeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                btnF1KeyTyped(evt);
            }
        });

        btnF6.setFont(new java.awt.Font("Arial", 1, 15)); // NOI18N
        btnF6.setText("  F6 - Cancelar Item");
        btnF6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        btnF6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnF6MouseClicked(evt);
            }
        });
        btnF6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnF6KeyPressed(evt);
            }
        });

        btnF12.setFont(new java.awt.Font("Arial", 1, 15)); // NOI18N
        btnF12.setText("  F12 - Sangra");
        btnF12.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel13.setText("Vendas:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(btnF1, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnF6, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnF12, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 428, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addComponent(txtVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnF1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnF6, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnF12, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel7.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        jLabel7.setText("Valor:");

        lblFotoItem.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        lblFotoItem.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtValorItem.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        txtValorItem.setText("R$ 0.00");

        lblUsuario.setFont(new java.awt.Font("Arial", 1, 17)); // NOI18N

        jLabel10.setFont(new java.awt.Font("Arial", 1, 17)); // NOI18N
        jLabel10.setText("Usuario:");

        jLabel8.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        jLabel8.setText("SubTotal:");

        txtSubtotalVenda.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        txtSubtotalVenda.setText("R$ 0.00");

        cmbQuantItem.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione a Quantidade", "1", "2", "3", "4", "5", "6", "7" }));
        cmbQuantItem.setToolTipText("");

        jLabel1.setFont(new java.awt.Font("Arial", 0, 17)); // NOI18N
        jLabel1.setText("Quantidade:");

        lblPesquisarCod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/imagens/search.png"))); // NOI18N
        lblPesquisarCod.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPesquisarCodMouseClicked(evt);
            }
        });

        lblPesquisarDescricao.setIcon(new javax.swing.ImageIcon(getClass().getResource("/lib/imagens/search.png"))); // NOI18N
        lblPesquisarDescricao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblPesquisarDescricaoMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(2, 2, 2)
                            .addComponent(cmbQuantItem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txtValorItem, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(1, 1, 1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addGap(11, 11, 11)
                                .addComponent(txtDescricaoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(71, 71, 71)
                                        .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblFotoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtSubtotalVenda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(9, 9, 9)
                                .addComponent(txtCodItem, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPesquisarCod)
                            .addComponent(lblPesquisarDescricao))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(13, 13, 13)
                        .addComponent(lblFotoItem, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbQuantItem, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtValorItem, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(1, 1, 1)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtSubtotalVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(1, 1, 1)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtCodItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(lblPesquisarCod, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                                .addComponent(txtDescricaoItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblPesquisarDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        setSize(new java.awt.Dimension(1076, 691));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

        // Definir a cor de fundo do painel de conteúdo como branca
        this.getContentPane().setBackground(Color.WHITE);

        // Definir a imagem de ícone da aplicação
        this.setIconImage(new ImageIcon(getClass().getResource("/lib/imagens/logo.png")).getImage());

        // Carregar e definir uma imagem para o lblLogo
        {
            // Carregar a imagem do recurso
            ImageIcon imagem = new ImageIcon(TelaPrincipal.class.getResource("/lib/imagens/logoauto1.png"));

            // Redimensionar a imagem para caber no lblLogo
            Image imag = imagem.getImage().getScaledInstance(lblLogo.getWidth(), lblLogo.getHeight(), Image.SCALE_DEFAULT);

            // Definir a imagem no lblLogo
            lblLogo.setIcon(new ImageIcon(imag));
        }

        // Criar um formatador de data para o formato "dd/MM/yyyy"
        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        // Definir o texto de lbData como a data formatada
        lbData.setText(dt.format(date));

        // Criar um temporizador para atualizar a data e hora
        Timer timer = new Timer(1000, new dataHora());
        timer.start();

        // Solicitar foco para txtCodItem
        txtCodItem.requestFocus();

    }//GEN-LAST:event_formWindowOpened

    private void btnF6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnF6MouseClicked
        // Exibe uma caixa de diálogo de confirmação Amarela
        int sair = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja Cancelar o Item?", "Atenção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        // Se o usuário escolher "Sim" na caixa de diálogo, cancela o item
        if (sair == JOptionPane.YES_OPTION) {
            cancelarItem();
        }
    }//GEN-LAST:event_btnF6MouseClicked

    private void btnF1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnF1MouseClicked
        // Exibe uma caixa de diálogo de confirmação amarela
        int sair = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja Finalizar a Venda?", "Atenção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        // Se o usuário escolher "Sim" na caixa de diálogo, finaliza a venda
        if (sair == JOptionPane.YES_OPTION) {
            finalizarVenda();
        }
    }//GEN-LAST:event_btnF1MouseClicked

    private void btnF1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnF1KeyTyped

    }//GEN-LAST:event_btnF1KeyTyped

    private void btnF1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnF1KeyPressed
//        // Verifica se a tecla F1 foi pressionada
//        if (evt.getKeyCode() == KeyEvent.VK_F1) {
//            // Exibe uma caixa de diálogo de confirmação amarela
//            int sair = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja Finalizar a Venda?", "Atenção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//
//            // Se o usuário escolher "Sim" na caixa de diálogo, finaliza a venda
//            if (sair == JOptionPane.YES_OPTION) {
//                finalizarVenda();
//            }
//        }
    }//GEN-LAST:event_btnF1KeyPressed

    private void btnF6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnF6KeyPressed
//        // TODO add your handling code here:
//        if (evt.getKeyCode() == KeyEvent.VK_F6) {
//            // Exibe uma caixa de diálogo de confirmação Vermelha
//            int sair = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja Cancelar o Item?", "Atenção", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//
//            // Se o usuário escolher "Sim" na caixa de diálogo, cancela o item
//            if (sair == JOptionPane.YES_OPTION) {
//                cancelarItem();
//            }
//        }
    }//GEN-LAST:event_btnF6KeyPressed

    private void lblPesquisarCodMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblPesquisarCodMouseClicked
        if (cmbQuantItem.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Escolha sua Quantidade");
            return;
        }

        if (txtCodItem.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Digite um Codigo do Produto!");
            return;
        }

        buscaItem("SELECT * FROM tb01_produtos WHERE tb01_cod_prod = ?", txtCodItem);
    }//GEN-LAST:event_lblPesquisarCodMouseClicked

    private void lblPesquisarDescricaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblPesquisarDescricaoMouseClicked
        if (cmbQuantItem.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(null, "Escolha sua Quantidade");
            return;
        }

        if (txtDescricaoItem.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "Digite a Descrição do Produto");
            return;
        }

        buscaItem("SELECT * FROM tb01_produtos WHERE tb01_Descricao = ?", txtDescricaoItem);
    }//GEN-LAST:event_lblPesquisarDescricaoMouseClicked

    class dataHora implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            Calendar now = Calendar.getInstance();
            lbHora.setText(String.format("%1$tH:%1$tM:%1$tS", now));
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new TelaPrincipal().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btnF1;
    private javax.swing.JLabel btnF12;
    private javax.swing.JLabel btnF6;
    private javax.swing.JComboBox<String> cmbQuantItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbData;
    private javax.swing.JLabel lbHora;
    private javax.swing.JLabel lblFotoItem;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblPesquisarCod;
    private javax.swing.JLabel lblPesquisarDescricao;
    public static javax.swing.JLabel lblUsuario;
    private javax.swing.JTable tbItem;
    private javax.swing.JTextField txtCodItem;
    private javax.swing.JTextField txtDescricaoItem;
    private javax.swing.JLabel txtSubtotalVenda;
    private javax.swing.JLabel txtValorItem;
    private javax.swing.JLabel txtVendas;
    // End of variables declaration//GEN-END:variables
}
