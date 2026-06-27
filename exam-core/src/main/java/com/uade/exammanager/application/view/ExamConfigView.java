package com.uade.exammanager.application.view;

import java.time.LocalDate;

public record ExamConfigView(String subjectName,
                             String teachers,
                             LocalDate examDate,
                             Integer pageCount,
                             boolean allowTopicRepetition,
                             Integer topicsPerGroup,
                             String headerImagePath) {
}
