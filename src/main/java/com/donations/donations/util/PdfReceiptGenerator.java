package com.donations.donations.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PdfReceiptGenerator {

    public static byte[] generateReceipt(String supporterEmail, String creatorName, String amount, String reference, String method) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            List<Long> offsets = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String emissionDate = LocalDateTime.now().format(formatter);
            String paymentMethod = (method != null && !method.isBlank()) ? method : "M-Pesa / e-Mola";

            // Header PDF
            write(out, "%PDF-1.4\n");

            // Obj 1: Catalog
            offsets.add((long) out.size());
            write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            // Obj 2: Pages
            offsets.add((long) out.size());
            write(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            // Obj 3: Page (A4: 595 x 842 pt)
            offsets.add((long) out.size());
            write(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n");

            // Obj 5: Helvetica (regular)
            offsets.add((long) out.size());
            write(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            // Obj 6: Helvetica-Bold
            offsets.add((long) out.size());
            write(out, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

            // Stream Content
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // Top Header Background (Dark slate/navy: 0.08, 0.12, 0.18)
            write(stream, "0.08 0.12 0.18 rg\n");
            write(stream, "0 730 595 112 re f\n");

            // Top Accent Bar (Teal: 0.18, 0.70, 0.55)
            write(stream, "0.18 0.70 0.55 rg\n");
            write(stream, "0 726 595 4 re f\n");

            // Header Text: Brand & Title
            write(stream, "BT\n");
            write(stream, "/F2 26 Tf\n");
            write(stream, "1 1 1 rg\n"); // White
            write(stream, "50 782 Td (APOIA) Tj\n");
            write(stream, "/F1 9 Tf\n");
            write(stream, "0.7 0.75 0.8 rg\n");
            write(stream, "50 766 Td (Plataforma Oficial de Doacoes e Apoio a Criadores) Tj\n");

            // Receipt Title on Top Right
            write(stream, "/F2 18 Tf\n");
            write(stream, "1 1 1 rg\n");
            write(stream, "360 782 Td (RECIBO DE DOACAO) Tj\n");
            write(stream, "/F1 10 Tf\n");
            write(stream, "0.85 0.88 0.92 rg\n");
            write(stream, "360 766 Td (REF: " + escapePdf(reference) + ") Tj\n");
            write(stream, "ET\n");

            // Card 1: Amount Banner (Light background)
            write(stream, "0.95 0.97 0.98 rg\n");
            write(stream, "50 635 495 65 re f\n");
            write(stream, "0.82 0.86 0.90 RG 1 w\n");
            write(stream, "50 635 495 65 re s\n");

            // Amount Text
            write(stream, "BT\n");
            write(stream, "/F1 10 Tf\n");
            write(stream, "0.4 0.45 0.5 rg\n");
            write(stream, "70 675 Td (VALOR TOTAL DOADO) Tj\n");
            write(stream, "/F2 24 Tf\n");
            write(stream, "0.08 0.55 0.38 rg\n"); // Emerald green
            write(stream, "70 648 Td (" + escapePdf(amount) + " MT) Tj\n");

            // Status Badge
            write(stream, "/F2 11 Tf\n");
            write(stream, "0.08 0.55 0.38 rg\n");
            write(stream, "400 660 Td ([ STATUS: LIQUIDADO ]) Tj\n");
            write(stream, "ET\n");

            // Section: Transaction Details
            write(stream, "BT\n");
            write(stream, "/F2 13 Tf\n");
            write(stream, "0.15 0.20 0.25 rg\n");
            write(stream, "50 595 Td (DETALHES DA TRANSACAO) Tj\n");
            write(stream, "ET\n");

            // Divider Line
            write(stream, "0.80 0.83 0.88 RG 1 w\n");
            write(stream, "50 585 m 545 585 l S\n");

            // Table Rows
            int y = 560;
            String[][] rows = new String[][] {
                {"Referencia da Transacao:", reference},
                {"Data e Hora da Emissao:", emissionDate},
                {"Metodo de Pagamento:", paymentMethod},
                {"Criador Beneficiario:", creatorName},
                {"Apoiante:", (supporterEmail != null && !supporterEmail.isBlank()) ? supporterEmail : "Anonimo"}
            };

            for (String[] row : rows) {
                // Background zebra stripe
                if ((y / 25) % 2 == 0) {
                    write(stream, "0.98 0.98 0.99 rg\n");
                    write(stream, "50 " + (y - 7) + " 495 24 re f\n");
                }

                write(stream, "BT\n");
                write(stream, "/F1 10 Tf\n");
                write(stream, "0.40 0.45 0.50 rg\n");
                write(stream, "65 " + y + " Td (" + escapePdf(row[0]) + ") Tj\n");

                write(stream, "/F2 10 Tf\n");
                write(stream, "0.12 0.16 0.22 rg\n");
                write(stream, "230 " + y + " Td (" + escapePdf(row[1]) + ") Tj\n");
                write(stream, "ET\n");

                write(stream, "0.90 0.92 0.94 RG 0.5 w\n");
                write(stream, "50 " + (y - 7) + " m 545 " + (y - 7) + " l S\n");

                y -= 26;
            }

            // Corporate Notice / Guarantee Box
            write(stream, "0.96 0.97 0.99 rg\n");
            write(stream, "50 340 495 65 re f\n");
            write(stream, "0.85 0.88 0.92 RG 1 w\n");
            write(stream, "50 340 495 65 re s\n");

            write(stream, "BT\n");
            write(stream, "/F2 10 Tf\n");
            write(stream, "0.18 0.24 0.32 rg\n");
            write(stream, "65 382 Td (COMPROVATIVO DE EMISSAO DIGITAL) Tj\n");
            write(stream, "/F1 9 Tf\n");
            write(stream, "0.45 0.50 0.55 rg\n");
            write(stream, "65 366 Td (Este documento constitui o comprovativo electronico de doacao processado atraves do APOIA.) Tj\n");
            write(stream, "65 352 Td (Os fundos foram creditados na conta do criador em conformidade com os termos da plataforma.) Tj\n");
            write(stream, "ET\n");

            // Footer Section
            write(stream, "0.82 0.85 0.88 RG 1 w\n");
            write(stream, "50 110 m 545 110 l S\n");

            write(stream, "BT\n");
            write(stream, "/F2 9 Tf\n");
            write(stream, "0.3 0.35 0.4 rg\n");
            write(stream, "50 92 Td (APOIA - Apoio a Criadores de Mocambique) Tj\n");
            write(stream, "/F1 8 Tf\n");
            write(stream, "0.55 0.6 0.65 rg\n");
            write(stream, "50 78 Td (Website: https://apoia.co.mz  |  Suporte: suporte@apoia.co.mz  |  Maputo, Mocambique) Tj\n");
            write(stream, "50 65 Td (Obrigado por fortalecer a comunidade de criadores independentes em Mocambique!) Tj\n");
            write(stream, "ET\n");

            byte[] streamBytes = stream.toByteArray();

            // Obj 4: Contents
            offsets.add((long) out.size());
            write(out, "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
            out.write(streamBytes);
            write(out, "\nendstream\nendobj\n");

            // Xref Table
            long xrefOffset = out.size();
            write(out, "xref\n");
            write(out, "0 " + (offsets.size() + 1) + "\n");
            write(out, "0000000000 65535 f \n");
            for (Long off : offsets) {
                write(out, String.format("%010d 00000 n \n", off));
            }

            // Trailer
            write(out, "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar recibo PDF", e);
        }
    }

    private static void write(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
    }

    private static String escapePdf(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replaceAll("[^\\x20-\\x7E]", "");
    }
}
