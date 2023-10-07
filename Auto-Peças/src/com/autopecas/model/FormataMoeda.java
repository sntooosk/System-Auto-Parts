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
package com.autopecas.model;

import java.text.DecimalFormat;
/**
 * Classe
 *
 * @author Juliano
 * @version 1.1
 */
public class FormataMoeda {
  
    public String ConvMoeda(String valTela) {
        double valor = Double.parseDouble(valTela);
        DecimalFormat df = new DecimalFormat("R$ #,##0.00");
        String formatado = df.format(valor);

        return formatado;
    }

    public String RemoverMascara(String valorFormatado) {
        // Remove caracteres não numéricos e converte a string formatada em um número
        String valorLimpo = valorFormatado.replaceAll("[^0-9]", "");
        double valor = Double.parseDouble(valorLimpo) / 100.0; // Divide por 100 para obter o valor correto

        return String.valueOf(valor);
    }
}
