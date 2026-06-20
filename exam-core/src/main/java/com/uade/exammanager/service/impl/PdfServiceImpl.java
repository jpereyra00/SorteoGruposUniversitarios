package com.uade.exammanager.service.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.uade.exammanager.entity.ExamConfig;
import com.uade.exammanager.entity.ExamGroup;
import com.uade.exammanager.entity.GroupMember;
import com.uade.exammanager.entity.GroupTopicAssignment;
import com.uade.exammanager.exception.BusinessException;
import com.uade.exammanager.repository.ExamGroupRepository;
import com.uade.exammanager.service.ExamConfigService;
import com.uade.exammanager.service.PdfService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PdfServiceImpl implements PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Font HEADER_UNIVERSITY_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font HEADER_FACULTY_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font HEADER_LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font HEADER_VALUE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 11, Font.BOLD);
    private static final Font BODY_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font BODY_BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);

    private final ExamGroupRepository examGroupRepository;
    private final ExamConfigService examConfigService;

    public PdfServiceImpl(ExamGroupRepository examGroupRepository, ExamConfigService examConfigService) {
        this.examGroupRepository = examGroupRepository;
        this.examConfigService = examConfigService;
    }

    @Override
    public byte[] generateGroupExamPdf(Long groupId) {
        ExamGroup group = examGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Grupo no encontrado."));
        ExamConfig config = examConfigService.getConfig();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 28, 28, 18, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            addInstitutionalHeader(document, config, group);
            addExamIntro(document, group, config);
            addGroupMembersSection(document, group);
            addAssignedTopicsSection(document, group);
            addEvaluationMethodologySection(document);
            addAcademicHonestySection(document);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("No se pudo generar el PDF del grupo.");
        }
    }

    private static final float HEADER_CELL_PADDING = 5f;

    private void addInstitutionalHeader(Document document, ExamConfig config, ExamGroup group) throws Exception {
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 3f, 2f});
        headerTable.setSpacingAfter(12f);

        String dateText = config.getExamDate() != null ? config.getExamDate().format(DATE_FORMATTER) : "";

        headerTable.addCell(createLogoCell());
        headerTable.addCell(createTitleCell());
        headerTable.addCell(createHojasCell(config.getPageCount()));

        PdfPCell materiaCell = createSingleLineCell("Materia:", safeText(config.getSubjectName()), true, 27f);
        materiaCell.setColspan(2);
        headerTable.addCell(materiaCell);
        headerTable.addCell(createSingleLineCell("Fecha:", dateText, true, 27f));

        PdfPCell nombreCell = createNombreCell(group);
        nombreCell.setColspan(2);
        headerTable.addCell(nombreCell);
        headerTable.addCell(createSingleLineCell("LU:", "________________", false, nombreCell.getMinimumHeight()));

        PdfPCell docentesCell = createSingleLineCell("Docentes:", safeText(config.getTeachers()), false, 34f);
        docentesCell.setColspan(2);
        headerTable.addCell(docentesCell);
        headerTable.addCell(createCalificacionCell());

        document.add(headerTable);
    }

    private PdfPCell createLogoCell() {
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.BOX);
        logoCell.setPadding(HEADER_CELL_PADDING);
        logoCell.setMinimumHeight(66f);
        logoCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        logoCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);

        Image logo = loadOfficialLogo();
        if (logo != null) {
            logo.scaleToFit(80f, 60f);
            logo.setAlignment(Image.ALIGN_CENTER);
            logoCell.addElement(logo);
        } else {
            Paragraph fallback = new Paragraph("UADE", new Font(Font.HELVETICA, 20, Font.BOLD, new Color(30, 58, 95)));
            fallback.setAlignment(Paragraph.ALIGN_CENTER);
            logoCell.addElement(fallback);
        }

        return logoCell;
    }

    private PdfPCell createTitleCell() {
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.BOX);
        titleCell.setPadding(HEADER_CELL_PADDING);
        titleCell.setMinimumHeight(66f);
        titleCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        titleCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);

        Paragraph university = new Paragraph("UNIVERSIDAD ARGENTINA DE LA EMPRESA", HEADER_UNIVERSITY_FONT);
        university.setAlignment(Paragraph.ALIGN_LEFT);
        university.setSpacingAfter(2f);
        titleCell.addElement(university);

        Paragraph faculty = new Paragraph("Facultad de Ingeniería y Ciencias Exactas", HEADER_FACULTY_FONT);
        faculty.setAlignment(Paragraph.ALIGN_LEFT);
        titleCell.addElement(faculty);
        return titleCell;
    }

    private PdfPCell createHojasCell(Integer pageCount) {
        String value = pageCount == null ? "" : pageCount.toString();
        Phrase phrase = new Phrase();
        phrase.add(new Chunk("Cantidad de Hojas:\n", HEADER_LABEL_FONT));
        phrase.add(new Chunk(value.isBlank() ? "_____" : value, HEADER_VALUE_FONT));

        PdfPCell hojasCell = new PdfPCell(phrase);
        hojasCell.setBorder(Rectangle.BOX);
        hojasCell.setPadding(HEADER_CELL_PADDING);
        hojasCell.setMinimumHeight(66f);
        hojasCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        hojasCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        return hojasCell;
    }

    private PdfPCell createNombreCell(ExamGroup group) {
        List<GroupMember> members = group.getMembers();
        StringBuilder nombresBuilder = new StringBuilder();

        if (members == null || members.isEmpty()) {
            nombresBuilder.append("________________");
        } else {
            for (int i = 0; i < members.size(); i++) {
                GroupMember member = members.get(i);
                String fullName = member.getStudent() != null ? safeText(member.getStudent().getFullName()) : "";
                String legajo = member.getStudent() != null ? safeText(member.getStudent().getLegajo()) : "";
                nombresBuilder.append(fullName).append(" (").append(legajo).append(")");
                if (i < members.size() - 1) {
                    nombresBuilder.append("\n");
                }
            }
        }

        Phrase phrase = new Phrase();
        phrase.add(new Chunk("Nombre:\n", HEADER_LABEL_FONT));
        phrase.add(new Chunk(nombresBuilder.toString(), HEADER_VALUE_FONT));

        PdfPCell nombreCell = new PdfPCell(phrase);
        nombreCell.setBorder(Rectangle.BOX);
        nombreCell.setPadding(HEADER_CELL_PADDING);
        nombreCell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        int memberCount = members == null ? 1 : Math.max(1, members.size());
        nombreCell.setMinimumHeight(22f + (memberCount * 14f));
        return nombreCell;
    }

    private PdfPCell createCalificacionCell() {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk("Calificación:\n\n", HEADER_LABEL_FONT));
        phrase.add(new Chunk("_____________", HEADER_VALUE_FONT));

        PdfPCell calificacionCell = new PdfPCell(phrase);
        calificacionCell.setBorder(Rectangle.BOX);
        calificacionCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
        calificacionCell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        calificacionCell.setPadding(HEADER_CELL_PADDING);
        calificacionCell.setMinimumHeight(40f);
        return calificacionCell;
    }

    private PdfPCell createSingleLineCell(String label, String value, boolean shaded, float minimumHeight) {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(label + " ", HEADER_LABEL_FONT));
        phrase.add(new Chunk(safeText(value), HEADER_VALUE_FONT));

        PdfPCell cell = new PdfPCell(phrase);
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(HEADER_CELL_PADDING);
        cell.setMinimumHeight(minimumHeight);
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
        if (shaded) {
            cell.setBackgroundColor(new Color(242, 242, 242));
        }
        return cell;
    }

    private void addExamIntro(Document document, ExamGroup group, ExamConfig config) throws DocumentException {
        Paragraph title = new Paragraph("Metodología de Evaluación del Primer Parcial (Colaborativo)", TITLE_FONT);
        title.setSpacingAfter(6f);
        document.add(title);

        document.add(new Paragraph("Asignatura: " + safeText(config.getSubjectName()), BODY_BOLD_FONT));
        document.add(new Paragraph("Actividad: Evaluación Teórico-Práctica Colaborativa", BODY_BOLD_FONT));
        document.add(new Paragraph("Grupo: " + safeText(group.getName()), BODY_BOLD_FONT));

        Paragraph objective = new Paragraph(
                "El objetivo de esta modalidad es evaluar tanto la comprensión conceptual como la capacidad de aplicación práctica y el estudio individual de todos los contenidos vistos hasta la Clase 6.",
                BODY_FONT
        );
        objective.setSpacingBefore(4f);
        objective.setSpacingAfter(8f);
        document.add(objective);
    }

    private void addGroupMembersSection(Document document, ExamGroup group) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Integrantes del Grupo", SUBTITLE_FONT);
        sectionTitle.setSpacingBefore(2f);
        sectionTitle.setSpacingAfter(4f);
        document.add(sectionTitle);

        List<GroupMember> members = group.getMembers().stream()
                .sorted(Comparator.comparing(m -> m.getStudent().getFullName(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (members.isEmpty()) {
            document.add(new Paragraph("- Sin integrantes asignados.", BODY_FONT));
        } else {
            com.lowagie.text.List membersList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            membersList.setIndentationLeft(12f);
            for (GroupMember member : members) {
                String line = member.getStudent().getFullName() + " - Legajo: " + member.getStudent().getLegajo();
                membersList.add(new com.lowagie.text.ListItem(line, BODY_FONT));
            }
            document.add(membersList);
        }

        document.add(Chunk.NEWLINE);
    }

    private void addAssignedTopicsSection(Document document, ExamGroup group) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Temas Asignados", SUBTITLE_FONT);
        sectionTitle.setSpacingAfter(4f);
        document.add(sectionTitle);

        List<GroupTopicAssignment> assignments = group.getTopicAssignments();
        if (assignments == null || assignments.isEmpty()) {
            document.add(new Paragraph("- Sin temas asignados.", BODY_FONT));
        } else {
            com.lowagie.text.List topicsList = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            topicsList.setIndentationLeft(12f);
            assignments.stream()
                    .map(a -> a.getTopic().getName())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(topic -> topicsList.add(new com.lowagie.text.ListItem(topic, BODY_FONT)));
            document.add(topicsList);
        }

        document.add(Chunk.NEWLINE);
    }

    private void addEvaluationMethodologySection(Document document) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Metodología de Evaluación", SUBTITLE_FONT);
        sectionTitle.setSpacingAfter(4f);
        document.add(sectionTitle);

        com.lowagie.text.List methodology = new com.lowagie.text.List(com.lowagie.text.List.ORDERED);
        methodology.setIndentationLeft(16f);
        methodology.add(new com.lowagie.text.ListItem("Desempeño Grupal (Exposición): 30% de la nota.", BODY_FONT));
        methodology.add(new com.lowagie.text.ListItem("Evaluación Individual (Formulario de Preguntas): 70% de la nota.", BODY_FONT));
        methodology.add(new com.lowagie.text.ListItem("Requisito de Aprobación: se requiere un mínimo de 60% del puntaje total.", BODY_FONT));
        methodology.add(new com.lowagie.text.ListItem("Criterio de Deshonestidad Académica: no está permitido el uso de herramientas de IA Generativa.", BODY_FONT));
        document.add(methodology);

        document.add(Chunk.NEWLINE);
    }

    private void addAcademicHonestySection(Document document) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("Cláusulas de Honestidad Académica", SUBTITLE_FONT);
        sectionTitle.setSpacingAfter(4f);
        document.add(sectionTitle);

        com.lowagie.text.List clauses = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        clauses.setIndentationLeft(12f);
        clauses.add(new com.lowagie.text.ListItem("Lea atentamente cada una de las preguntas para asegurarse de responder exactamente lo que se solicita.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("Piense y elabore su respuesta de forma tal que la misma sea comprensible.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("Se evaluará tanto el conocimiento como la claridad de la exposición (incluida ortografía y letra legible).", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("La interpretación de las consignas forma parte del examen y de su nota final.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("Cada pregunta tiene su puntuación, según indicación del profesor, exigiéndose una nota mínima de 4 para poder aprobar.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("No se permite el uso del celular ni de ningún otro artefacto tecnológico durante el examen (salvo calculadora).", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("No se permite consultar libros, cuadernos o apuntes durante el examen.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("No se permite hablar con sus compañeros ni salir del aula durante el examen.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("Recuerde que la honestidad académica contribuye a su formación personal y si la infringe, recursará la materia recibiendo una sanción.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("Duración del examen: 120 minutos.", BODY_FONT));
        clauses.add(new com.lowagie.text.ListItem("ESTE EXAMEN SERÁ CONSIDERADO NULO SI EL ALUMNO, A LA FECHA DE RENDIRLO, NO CUMPLE CON LO ESTABLECIDO EN LA NORMATIVA DE LA UNIVERSIDAD.", BODY_BOLD_FONT));
        document.add(clauses);
    }

    private Image loadOfficialLogo() {
        try {
            ClassPathResource officialLogo = new ClassPathResource("static/images/LogoUADE.png");
            if (officialLogo.exists()) {
                try (InputStream in = officialLogo.getInputStream()) {
                    return Image.getInstance(in.readAllBytes());
                }
            }

            ClassPathResource fallbackLogo = new ClassPathResource("static/images/default-header.png");
            if (fallbackLogo.exists()) {
                try (InputStream in = fallbackLogo.getInputStream()) {
                    return Image.getInstance(in.readAllBytes());
                }
            }
        } catch (Exception ignored) {
            // Si falla la carga de imagen, se usa texto institucional en la celda.
        }
        return null;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
