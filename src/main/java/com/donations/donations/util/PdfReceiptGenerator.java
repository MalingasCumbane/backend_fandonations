package com.donations.donations.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PdfReceiptGenerator {

    public static byte[] generateReceipt(String supporterEmail, String creatorName, String amount, String reference, String method) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String emissionDate = LocalDateTime.now().format(formatter);
            String paymentMethod = (method != null && !method.isBlank()) ? method : "M-Pesa / e-Mola";
            String supporter = (supporterEmail != null && !supporterEmail.isBlank()) ? supporterEmail : "Anonimo";
            String creator = (creatorName != null && !creatorName.isBlank()) ? creatorName : "Criador";

            // 1. Prepare stream content first so we know its exact length
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // Top Header Background (Dark slate/navy: 0.08, 0.12, 0.18)
            write(stream, "0.08 0.12 0.18 rg\n");
            write(stream, "0 730 595 112 re f\n");

            // Top Accent Bar (Teal: 0.18, 0.70, 0.55)
            write(stream, "0.18 0.70 0.55 rg\n");
            write(stream, "0 726 595 4 re f\n");

            // Header Brand & Subtitle
            write(stream, "BT /F2 24 Tf 1 1 1 rg 50 778 Td (TXOPA) Tj ET\n");
            write(stream, "BT /F1 9 Tf 0.75 0.82 0.90 rg 50 760 Td (Plataforma Oficial de Apoio a Criadores de Mocambique) Tj ET\n");

            // Header Receipt & Ref
            write(stream, "BT /F2 16 Tf 1 1 1 rg 340 778 Td (RECIBO DE DOACAO) Tj ET\n");
            write(stream, "BT /F1 9 Tf 0.75 0.82 0.90 rg 340 760 Td (REF: " + escapePdf(reference) + ") Tj ET\n");

            // Amount Card Background & Border
            write(stream, "0.95 0.97 0.98 rg 50 630 495 68 re f\n");
            write(stream, "0.82 0.86 0.90 RG 1 w 50 630 495 68 re s\n");

            // Amount Card Content
            write(stream, "BT /F1 9 Tf 0.40 0.45 0.50 rg 70 674 Td (VALOR TOTAL DOADO) Tj ET\n");
            write(stream, "BT /F2 22 Tf 0.08 0.55 0.35 rg 70 648 Td (" + escapePdf(amount) + " MT) Tj ET\n");

            // Status Badge (Green pill)
            write(stream, "0.85 0.96 0.90 rg 375 647 150 26 re f\n");
            write(stream, "0.15 0.65 0.40 RG 1 w 375 647 150 26 re s\n");
            write(stream, "BT /F2 10 Tf 0.08 0.55 0.35 rg 395 656 Td (STATUS: LIQUIDADO) Tj ET\n");

            // Section: Transaction Details
            write(stream, "BT /F2 12 Tf 0.15 0.20 0.28 rg 50 595 Td (DETALHES DA TRANSACAO) Tj ET\n");
            write(stream, "0.80 0.83 0.88 RG 1 w 50 585 m 545 585 l S\n");

            // Table Rows
            int y = 556;
            String[][] rows = new String[][]{
                    {"Referencia da Transacao:", reference},
                    {"Data e Hora da Emissao:", emissionDate},
                    {"Metodo de Pagamento:", paymentMethod},
                    {"Criador Beneficiario:", creator},
                    {"Apoiante:", supporter}
            };

            for (int i = 0; i < rows.length; i++) {
                String[] row = rows[i];
                if (i % 2 == 0) {
                    write(stream, "0.97 0.98 0.99 rg 50 " + (y - 7) + " 495 24 re f\n");
                }

                write(stream, "BT /F1 10 Tf 0.40 0.45 0.50 rg 65 " + y + " Td (" + escapePdf(row[0]) + ") Tj ET\n");
                write(stream, "BT /F2 10 Tf 0.12 0.16 0.22 rg 220 " + y + " Td (" + escapePdf(row[1]) + ") Tj ET\n");

                write(stream, "0.90 0.92 0.94 RG 0.5 w 50 " + (y - 7) + " m 545 " + (y - 7) + " l S\n");
                y -= 26;
            }

            // Notice Box
            write(stream, "0.96 0.97 0.99 rg 50 350 495 62 re f\n");
            write(stream, "0.85 0.88 0.92 RG 1 w 50 350 495 62 re s\n");

            write(stream, "BT /F2 10 Tf 0.18 0.24 0.32 rg 65 392 Td (COMPROVATIVO DE EMISSAO DIGITAL) Tj ET\n");
            write(stream, "BT /F1 9 Tf 0.45 0.50 0.55 rg 65 376 Td (Este documento constitui o comprovativo electronico de doacao processado atraves do Txopa.) Tj ET\n");
            write(stream, "BT /F1 9 Tf 0.45 0.50 0.55 rg 65 362 Td (Os fundos foram creditados na conta do criador em conformidade com os termos da plataforma.) Tj ET\n");

            // Footer Section
            write(stream, "0.82 0.85 0.88 RG 1 w 50 110 m 545 110 l S\n");
            write(stream, "BT /F2 9 Tf 0.30 0.35 0.40 rg 50 92 Td (Txopa - Apoio a Criadores de Mocambique) Tj ET\n");
            write(stream, "BT /F1 8 Tf 0.55 0.60 0.65 rg 50 78 Td (Website: https://txopa.app  |  Suporte: suporte@txopa.app  |  Maputo, Mocambique) Tj ET\n");
            write(stream, "BT /F1 8 Tf 0.55 0.60 0.65 rg 50 65 Td (Obrigado por fortalecer a comunidade de criadores independentes em Mocambique!) Tj ET\n");

            byte[] streamBytes = stream.toByteArray();

            // Array to store exact byte offsets for each object (1 to 6)
            long[] offsets = new long[7];

            // Header PDF
            write(out, "%PDF-1.4\n");

            // Obj 1: Catalog
            offsets[1] = out.size();
            write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            // Obj 2: Pages
            offsets[2] = out.size();
            write(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            // Obj 3: Page (A4: 595 x 842 pt)
            offsets[3] = out.size();
            write(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n");

            // Obj 4: Contents (Stream)
            offsets[4] = out.size();
            write(out, "4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
            out.write(streamBytes);
            write(out, "\nendstream\nendobj\n");

            // Obj 5: Helvetica (regular)
            offsets[5] = out.size();
            write(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            // Obj 6: Helvetica-Bold
            offsets[6] = out.size();
            write(out, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

            // Xref Table (Object 0 to 6 = 7 entries)
            long xrefOffset = out.size();
            write(out, "xref\r\n");
            write(out, "0 7\r\n");
            write(out, "0000000000 65535 f \r\n");
            for (int i = 1; i <= 6; i++) {
                write(out, String.format("%010d 00000 n \r\n", offsets[i]));
            }

            // Trailer
            write(out, "trailer\r\n<< /Size 7 /Root 1 0 R >>\r\nstartxref\r\n" + xrefOffset + "\r\n%%EOF\r\n");

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
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[^\\x20-\\x7E]", "");
    }
}
