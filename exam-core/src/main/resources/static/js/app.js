const state = {
    students: [],
    groups: [],
    topics: [],
    config: null,
    navKey: 'students',
    manualAssignments: new Map(),
    rouletteSkipRequested: false,
    rouletteSoundEnabled: true
};


const ROULETTE_COLORS = ['#FF4D6D', '#FFB703', '#3A86FF', '#06D6A0', '#8338EC', '#FB5607', '#00B4D8', '#80ED99'];
const ROULETTE_GROUP_EMOJIS = ['😊', '😃', '🎉', '🌟', '😄', '🎊', '💫', '✨'];
const ROULETTE_TOPIC_EMOJIS = ['📚', '📖', '📝', '📋', '📄', '📑', '🎓', '📘'];
const rouletteAudio = {
    bgm: null,
    win: null,
    confetti: null,
    volume: 0.55
};
const dashboardMeta = {
    students: { title: 'Dashboard de Estudiantes', breadcrumb: 'Estudiantes' },
    groups: { title: 'Dashboard de Grupos', breadcrumb: 'Grupos' },
    topics: { title: 'Dashboard de Temas', breadcrumb: 'Temas' },
    config: { title: 'Configuración de Examen', breadcrumb: 'Configuración' },
    pdfs: { title: 'Generación de PDFs', breadcrumb: 'PDFs' }
};

document.addEventListener('DOMContentLoaded', () => {
    wireNavigation();
    wireStudentsDashboard();
    wireGroupsDashboard();
    wireTopicsDashboard();
    wireConfigDashboard();
    wirePdfsDashboard();
    wireGlobalActions();
    wireRouletteControls();

    bootstrap.Tooltip.getOrCreateInstance(document.body, { selector: '[data-bs-toggle="tooltip"]' });

    restorePdfHistory();
    loadDashboardData();
});

async function loadDashboardData() {
    try {
        const data = await apiFetch('/api/dashboard');
        state.students = data.students || [];
        state.groups = data.groups || [];
        state.topics = data.topics || [];
        state.config = data.config || {};
        renderAll();
    } catch (error) {
        showError(error.message || 'No se pudo cargar el dashboard inicial.');
    }
}

function renderAll() {
    renderStudentsTable();
    renderGroupsResult();
    renderTopicsList();
    syncConfigForm();
    renderTopicAssignmentsPreview();
    renderPdfGroups();
    updateRandomDistributionPreview();
}

function wireNavigation() {
    const nav = document.getElementById('dashboardNav');
    nav.querySelectorAll('[data-dashboard]').forEach((button) => {
        button.addEventListener('click', () => setActiveDashboard(button.dataset.dashboard));
    });

    const hash = window.location.hash.replace('#', '');
    if (dashboardMeta[hash]) {
        setActiveDashboard(hash);
    }
}

function setActiveDashboard(key) {
    state.navKey = key;
    window.location.hash = key;

    document.querySelectorAll('#dashboardNav .nav-link').forEach((el) => {
        el.classList.toggle('active', el.dataset.dashboard === key);
    });

    document.querySelectorAll('.dashboard-section').forEach((section) => {
        section.classList.toggle('show', section.id === `dashboard-${key}`);
    });

    const meta = dashboardMeta[key] || dashboardMeta.students;
    document.getElementById('currentDashboardTitle').textContent = meta.title;
    document.getElementById('dynamicBreadcrumb').innerHTML = `
      <li class="breadcrumb-item"><i class="bi bi-house-door-fill"></i></li>
      <li class="breadcrumb-item active" aria-current="page">${meta.breadcrumb}</li>
    `;
}

function wireGlobalActions() {
    document.getElementById('refreshAllBtn').addEventListener('click', loadDashboardData);
}

function wireStudentsDashboard() {
    const form = document.getElementById('studentForm');
    const cancelBtn = document.getElementById('cancelStudentEditBtn');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!validateStudentForm()) return;

        const payload = {
            fullName: document.getElementById('studentFullName').value.trim(),
            legajo: document.getElementById('studentLegajo').value.trim()
        };

        const id = document.getElementById('studentId').value;
        const endpoint = id ? `/api/students/${id}` : '/api/students';
        const method = id ? 'PUT' : 'POST';

        await withLoading('saveStudentBtn', async () => {
            const result = await apiFetch(endpoint, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            showToast(result.message || 'Operación guardada.', 'success');
            resetStudentForm();
            await loadDashboardData();
        });
    });

    cancelBtn.addEventListener('click', resetStudentForm);

    document.getElementById('importTextBtn').addEventListener('click', async () => {
        const bulkText = document.getElementById('bulkTextInput').value.trim();
        if (!bulkText) {
            return showError('Ingresá texto para importar estudiantes.');
        }

        await withLoading('importTextBtn', async () => {
            const result = await apiFetch('/api/students/import-text', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bulkText })
            });
            showToast(`${result.importedCount} estudiantes importados/actualizados.`, 'success');
            document.getElementById('bulkTextInput').value = '';
            await loadDashboardData();
        });
    });

    document.getElementById('importCsvBtn').addEventListener('click', async () => {
        const csvInput = document.getElementById('csvFileInput');
        if (!csvInput.files.length) {
            return showError('Seleccioná un archivo CSV.');
        }

        const formData = new FormData();
        formData.append('csvFile', csvInput.files[0]);

        await withLoading('importCsvBtn', async () => {
            const result = await apiFetch('/api/students/import-csv', {
                method: 'POST',
                body: formData
            });
            showToast(`${result.importedCount} estudiantes importados desde CSV.`, 'success');
            csvInput.value = '';
            await loadDashboardData();
        });
    });
}

function renderStudentsTable() {
    const tbody = document.querySelector('#studentsTable tbody');
    const countBadge = document.getElementById('studentsCountBadge');
    countBadge.textContent = state.students.length;

    if (!state.students.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="text-muted">No hay estudiantes cargados.</td></tr>';
        return;
    }

    tbody.innerHTML = state.students.map((student) => `
      <tr>
        <td>${escapeHtml(student.fullName)}</td>
        <td>${escapeHtml(student.legajo)}</td>
        <td class="text-end">
            <div class="btn-group btn-group-sm">
              <button class="btn btn-outline-primary" data-action="edit" data-id="${student.id}"><i class="bi bi-pencil-fill"></i></button>
              <button class="btn btn-outline-warning" data-action="release" data-id="${student.id}"><i class="bi bi-person-dash-fill"></i></button>
              <button class="btn btn-outline-danger" data-action="delete" data-id="${student.id}"><i class="bi bi-trash-fill"></i></button>
            </div>
        </td>
      </tr>
    `).join('');

    tbody.querySelectorAll('button[data-action]').forEach((button) => {
        button.addEventListener('click', () => handleStudentRowAction(button.dataset.action, Number(button.dataset.id)));
    });
}

async function handleStudentRowAction(action, studentId) {
    const student = state.students.find((s) => s.id === studentId);
    if (!student) return;

    if (action === 'edit') {
        document.getElementById('studentId').value = student.id;
        document.getElementById('studentFullName').value = student.fullName;
        document.getElementById('studentLegajo').value = student.legajo;
        document.getElementById('cancelStudentEditBtn').classList.remove('d-none');
        document.getElementById('saveStudentBtn').textContent = 'Actualizar estudiante';
        return;
    }

    if (action === 'delete' && !window.confirm(`¿Eliminar a ${student.fullName}?`)) return;
    if (action === 'release' && !window.confirm(`¿Liberar a ${student.fullName} de su grupo?`)) return;

    try {
        if (action === 'delete') {
            await apiFetch(`/api/students/${studentId}`, { method: 'DELETE' });
            showToast('Estudiante eliminado.', 'success');
        }

        if (action === 'release') {
            await apiFetch(`/api/groups/release-student/${studentId}`, { method: 'POST' });
            showToast('Estudiante liberado de su grupo.', 'success');
        }

        await loadDashboardData();
    } catch (error) {
        showError(error.message);
    }
}

function validateStudentForm() {
    const fullNameInput = document.getElementById('studentFullName');
    const legajoInput = document.getElementById('studentLegajo');

    const validName = fullNameInput.value.trim().length > 0;
    const validLegajo = legajoInput.value.trim().length > 0;

    fullNameInput.classList.toggle('is-invalid', !validName);
    legajoInput.classList.toggle('is-invalid', !validLegajo);

    return validName && validLegajo;
}

function resetStudentForm() {
    document.getElementById('studentForm').reset();
    document.getElementById('studentId').value = '';
    document.getElementById('cancelStudentEditBtn').classList.add('d-none');
    document.getElementById('saveStudentBtn').textContent = 'Guardar estudiante';
    document.getElementById('studentFullName').classList.remove('is-invalid');
    document.getElementById('studentLegajo').classList.remove('is-invalid');
}

function wireGroupsDashboard() {
    const modeRandom = document.getElementById('modeRandom');
    const modeManual = document.getElementById('modeManual');
    const randomGroupsInput = document.getElementById('randomGroupsCount');
    const randomMembersInput = document.getElementById('randomMembersPerGroup');

    modeRandom.addEventListener('change', syncGroupModeLock);
    modeManual.addEventListener('change', syncGroupModeLock);

    randomGroupsInput.addEventListener('input', updateRandomDistributionPreview);
    randomMembersInput.addEventListener('input', updateRandomDistributionPreview);

    document.getElementById('buildManualInterfaceBtn').addEventListener('click', renderManualAssignmentBuilder);

    document.getElementById('generateRandomGroupsBtn').addEventListener('click', async () => {
        const groupsCount = Number(randomGroupsInput.value);
        const membersPerGroup = Number(randomMembersInput.value);

        if (!validateRandomInputs(groupsCount, membersPerGroup)) return;

        await withLoading('generateRandomGroupsBtn', async () => {
            const previewNames = state.students.map((s) => s.fullName);
            await runRouletteSequence({
                title: 'Sorteo de Grupos',
                items: previewNames,
                rounds: Math.max(1, groupsCount),
                statusPrefix: 'Formando grupo',
                emojiSet: ROULETTE_GROUP_EMOJIS
            });

            const result = await apiFetch('/api/groups/random', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ groupsCount, membersPerGroup })
            });

            showToast(result.message, 'success');
            await loadDashboardData();
            updateRandomDistributionPreview();
        });
    });

    document.getElementById('saveManualGroupsBtn').addEventListener('click', async () => {
        const groupsCount = Number(document.getElementById('manualGroupsCount').value);
        if (!groupsCount || groupsCount < 1) {
            return showError('Ingresá una cantidad de grupos válida para asignación manual.');
        }
        if (state.manualAssignments.size !== state.students.length) {
            return showError('Todos los estudiantes deben estar asignados a un grupo.');
        }

        const assignments = state.students.map((student) => ({
            studentId: student.id,
            groupNumber: Number(state.manualAssignments.get(student.id))
        }));

        const emptyGroup = Array.from({ length: groupsCount }, (_, i) => i + 1)
            .find((groupNumber) => !assignments.some((a) => a.groupNumber === groupNumber));

        if (emptyGroup) {
            return showError(`No se permiten grupos vacíos. El grupo ${emptyGroup} no tiene integrantes.`);
        }

        await withLoading('saveManualGroupsBtn', async () => {
            const result = await apiFetch('/api/groups/manual', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ groupsCount, assignments })
            });
            showToast(result.message, 'success');
            await loadDashboardData();
            updateRandomDistributionPreview();
        });
    });

    document.getElementById('clearGroupsBtn').addEventListener('click', async () => {
        if (!window.confirm('Esta acción eliminará todos los grupos y sus asignaciones. ¿Continuar?')) return;

        await withLoading('clearGroupsBtn', async () => {
            const result = await apiFetch('/api/groups/clear', { method: 'POST' });
            showToast(result.message, 'success');
            await loadDashboardData();
            updateRandomDistributionPreview();
        });
    });

    syncGroupModeLock();
    updateRandomDistributionPreview();
}

function syncGroupModeLock() {
    const randomMode = document.getElementById('modeRandom').checked;

    const randomPanel = document.getElementById('randomModePanel');
    const manualPanel = document.getElementById('manualModePanel');

    randomPanel.classList.toggle('d-none', !randomMode);
    manualPanel.classList.toggle('d-none', randomMode);

    document.getElementById('generateRandomGroupsBtn').disabled = !randomMode;
    document.getElementById('buildManualInterfaceBtn').disabled = randomMode;

    const manualContainer = document.getElementById('manualAssignmentContainer');
    const manualEmpty = document.getElementById('manualAssignmentEmpty');
    if (randomMode) {
        manualContainer.classList.add('d-none');
        manualEmpty.classList.remove('d-none');
        document.getElementById('saveManualGroupsBtn').disabled = true;
    }
}

function validateRandomInputs(groupsCount, membersPerGroup) {
    const totalStudents = state.students.length;
    if (!totalStudents) {
        showError('No hay estudiantes cargados para generar grupos.');
        return false;
    }

    if (groupsCount <= 0 || membersPerGroup <= 0) {
        showError('La cantidad de grupos e integrantes debe ser mayor a 0.');
        return false;
    }

    if (groupsCount > totalStudents) {
        showError(`No podés crear ${groupsCount} grupos con solo ${totalStudents} estudiantes.`);
        return false;
    }

    if (membersPerGroup > totalStudents) {
        showError(`La sugerencia de integrantes por grupo (${membersPerGroup}) no puede superar los ${totalStudents} estudiantes disponibles.`);
        return false;
    }

    return true;
}

function calculateDistribution(totalStudents, groupsCount) {
    if (!totalStudents || !groupsCount || groupsCount < 1 || groupsCount > totalStudents) {
        return null;
    }

    const baseSize = Math.floor(totalStudents / groupsCount);
    const groupsWithExtra = totalStudents % groupsCount;
    const groupSizes = Array.from({ length: groupsCount }, (_, index) => baseSize + (index < groupsWithExtra ? 1 : 0));

    return {
        baseSize,
        groupsWithExtra,
        groupSizes,
        minSize: Math.min(...groupSizes),
        maxSize: Math.max(...groupSizes)
    };
}

function updateRandomDistributionPreview() {
    const groupsCount = Number(document.getElementById('randomGroupsCount').value);
    const membersPerGroup = Number(document.getElementById('randomMembersPerGroup').value);
    const totalStudents = state.students.length;

    const infoBox = document.getElementById('randomDistributionInfo');
    const badge = document.getElementById('randomDistributionBadge');

    if (!infoBox || !badge) return;

    if (!totalStudents) {
        infoBox.innerHTML = '<span class="text-muted">Primero cargá estudiantes para ver la distribución automática.</span>';
        badge.className = 'badge text-bg-secondary';
        badge.textContent = 'Sin datos';
        return;
    }

    if (!groupsCount || groupsCount < 1) {
        infoBox.innerHTML = '<span class="text-warning">Ingresá una cantidad válida de grupos.</span>';
        badge.className = 'badge text-bg-warning';
        badge.textContent = 'Configurar';
        return;
    }

    if (groupsCount > totalStudents) {
        infoBox.innerHTML = `⚠️ Configuración inválida: ${groupsCount} grupos para ${totalStudents} estudiantes generaría grupos vacíos.`;
        badge.className = 'badge text-bg-danger';
        badge.textContent = 'Inválido';
        return;
    }

    if (!membersPerGroup || membersPerGroup < 1) {
        infoBox.innerHTML = '<span class="text-warning">Ingresá una sugerencia válida de integrantes por grupo.</span>';
        badge.className = 'badge text-bg-warning';
        badge.textContent = 'Configurar';
        return;
    }

    if (membersPerGroup > totalStudents) {
        infoBox.innerHTML = `⚠️ La sugerencia de ${membersPerGroup} integrantes supera el total (${totalStudents}).`;
        badge.className = 'badge text-bg-danger';
        badge.textContent = 'Inválido';
        return;
    }

    const distribution = calculateDistribution(totalStudents, groupsCount);
    if (!distribution) {
        badge.className = 'badge text-bg-warning';
        badge.textContent = 'Configurar';
        return;
    }

    const chunks = {};
    distribution.groupSizes.forEach((size) => {
        chunks[size] = (chunks[size] || 0) + 1;
    });

    const breakdown = Object.entries(chunks)
        .sort((a, b) => Number(b[0]) - Number(a[0]))
        .map(([size, count]) => `• ${count} grupo(s) de ${size} integrante(s)`)
        .join('<br>');

    const suggestedGroups = Math.max(1, Math.round(totalStudents / Math.max(1, membersPerGroup)));
    const suggestion = suggestedGroups !== groupsCount
        ? `<br><span class="text-muted">Sugerencia: si querés aproximarte a ${membersPerGroup} por grupo, probá con ${suggestedGroups} grupo(s).</span>`
        : '';

    infoBox.innerHTML = `
        <div><strong>Distribución automática:</strong> ${totalStudents} estudiantes en ${groupsCount} grupos.</div>
        <div class="small mt-1">${breakdown}</div>
        <div class="small mt-1 text-success">Balanceado: diferencia máxima de ${distribution.maxSize - distribution.minSize} estudiante(s) entre grupos.</div>
        ${suggestion}
    `;

    const isIdeal = membersPerGroup >= distribution.minSize && membersPerGroup <= distribution.maxSize;
    badge.className = `badge ${isIdeal ? 'text-bg-success' : 'text-bg-info'}`;
    badge.textContent = isIdeal ? 'Óptimo' : 'Ajustado';
}

function renderManualAssignmentBuilder() {
    const groupsCount = Number(document.getElementById('manualGroupsCount').value);
    if (!groupsCount || groupsCount < 1) {
        return showError('Ingresá una cantidad de grupos válida.');
    }
    if (!state.students.length) {
        return showError('No hay estudiantes para asignar.');
    }

    state.manualAssignments.clear();

    const tbody = document.querySelector('#manualAssignmentTable tbody');
    tbody.innerHTML = state.students.map((student) => `
      <tr>
        <td>${escapeHtml(student.fullName)}</td>
        <td>${escapeHtml(student.legajo)}</td>
        <td>
            <select class="form-select form-select-sm manual-assignment-select" data-student-id="${student.id}">
              ${Array.from({ length: groupsCount }, (_, i) => `<option value="${i + 1}">Grupo ${i + 1}</option>`).join('')}
            </select>
        </td>
      </tr>
    `).join('');

    tbody.querySelectorAll('.manual-assignment-select').forEach((select) => {
        const studentId = Number(select.dataset.studentId);
        state.manualAssignments.set(studentId, Number(select.value));

        select.addEventListener('change', () => {
            state.manualAssignments.set(studentId, Number(select.value));
            renderManualGroupsPreview(groupsCount);
        });
    });

    document.getElementById('manualAssignmentEmpty').classList.add('d-none');
    document.getElementById('manualAssignmentContainer').classList.remove('d-none');
    document.getElementById('saveManualGroupsBtn').disabled = false;
    renderManualGroupsPreview(groupsCount);
}

function renderManualGroupsPreview(groupsCount) {
    const container = document.getElementById('manualGroupsPreview');
    const buckets = Array.from({ length: groupsCount }, (_, i) => ({
        label: `Grupo ${i + 1}`,
        members: []
    }));

    state.students.forEach((student) => {
        const groupNumber = Number(state.manualAssignments.get(student.id));
        if (groupNumber > 0 && groupNumber <= groupsCount) {
            buckets[groupNumber - 1].members.push(student.fullName);
        }
    });

    container.innerHTML = buckets.map((bucket) => `
      <div class="manual-group-card">
        <strong>${bucket.label}</strong>
        <ul class="small mb-0 mt-1">
            ${bucket.members.length ? bucket.members.map((name) => `<li>${escapeHtml(name)}</li>`).join('') : '<li class="text-muted">Sin integrantes</li>'}
        </ul>
      </div>
    `).join('');
}

function renderGroupsResult() {
    const container = document.getElementById('groupsResultContainer');
    if (!state.groups.length) {
        container.innerHTML = '<div class="text-muted">Todavía no hay grupos generados.</div>';
        return;
    }

    container.innerHTML = `<div class="group-grid">
      ${state.groups.map((group) => `
        <article class="group-card">
            <h6 class="mb-2">${escapeHtml(group.name)}</h6>
            <div class="small text-muted">Integrantes</div>
            <ul class="small mb-2">
              ${(group.members || []).map((member) => `<li>${escapeHtml(member.fullName)} (${escapeHtml(member.legajo)})</li>`).join('') || '<li class="text-muted">Sin integrantes</li>'}
            </ul>
            <div class="small text-muted">Temas asignados</div>
            <div>
              ${(group.topics || []).map((topic) => `<span class="topic-chip">${escapeHtml(topic.topicName)}</span>`).join('') || '<span class="text-muted small">Sin temas</span>'}
            </div>
        </article>
      `).join('')}
    </div>`;
}

function wireTopicsDashboard() {
    document.getElementById('topicForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const topicName = document.getElementById('topicNameInput').value.trim();
        if (!topicName) {
            return showError('Ingresá un nombre de tema.');
        }

        await withLoading('addTopicBtn', async () => {
            await apiFetch('/api/topics', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ topicName })
            });
            document.getElementById('topicNameInput').value = '';
            showToast('Tema agregado.', 'success');
            await loadDashboardData();
        });
    });

    document.getElementById('assignTopicsBtn').addEventListener('click', async () => {
        const topicsPerGroup = Number(document.getElementById('topicsPerGroupInput').value);
        const allowRepetition = document.getElementById('allowRepetitionInput').checked;

        if (!topicsPerGroup || topicsPerGroup < 1) {
            return showError('La cantidad de temas por grupo debe ser mayor a 0.');
        }
        if (!state.groups.length) {
            return showError('Primero generá grupos.');
        }
        if (!state.topics.length) {
            return showError('Primero cargá temas.');
        }

        await withLoading('assignTopicsBtn', async () => {
            const topicNames = state.topics.map((t) => t.name);
            await runRouletteSequence({
                title: 'Sorteo de Temas',
                items: topicNames,
                rounds: Math.max(1, state.groups.length),
                statusPrefix: 'Asignando temas para grupo',
                emojiSet: ROULETTE_TOPIC_EMOJIS
            });

            const result = await apiFetch('/api/topics/assign', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ topicsPerGroup, allowRepetition })
            });

            showToast(result.message || 'Temas asignados.', 'success');
            await loadDashboardData();
        });
    });
}

function renderTopicsList() {
    const list = document.getElementById('topicsList');
    if (!state.topics.length) {
        list.innerHTML = '<li class="list-group-item text-muted">No hay temas cargados.</li>';
        return;
    }

    list.innerHTML = state.topics.map((topic) => `
      <li class="list-group-item d-flex justify-content-between align-items-center">
        <span>${escapeHtml(topic.name)}</span>
        <button class="btn btn-sm btn-outline-danger" data-topic-delete="${topic.id}"><i class="bi bi-trash-fill"></i></button>
      </li>
    `).join('');

    list.querySelectorAll('[data-topic-delete]').forEach((button) => {
        button.addEventListener('click', async () => {
            const topicId = Number(button.dataset.topicDelete);
            if (!window.confirm('¿Eliminar este tema?')) return;
            await apiFetch(`/api/topics/${topicId}`, { method: 'DELETE' });
            showToast('Tema eliminado.', 'success');
            await loadDashboardData();
        });
    });
}

function renderTopicAssignmentsPreview() {
    const preview = document.getElementById('topicAssignmentResult');
    preview.innerHTML = state.groups.map((group) => `
      <div class="small mb-2">
        <strong>${escapeHtml(group.name)}:</strong>
        ${(group.topics || []).map((topic) => `<span class="topic-chip">${escapeHtml(topic.topicName)}</span>`).join('') || '<span class="text-muted">sin temas</span>'}
      </div>
    `).join('') || '<div class="text-muted small">Sin asignaciones todavía.</div>';
}

function wireConfigDashboard() {
    const form = document.getElementById('examConfigForm');
    const headerInput = document.getElementById('headerImageInput');

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!form.reportValidity()) return;

        const formData = new FormData(form);
        const examDate = document.getElementById('examDateInput').value;
        if (!examDate) formData.delete('examDate');

        await withLoading('configSaveBtn', async () => {
            const result = await apiFetch('/api/exam-config', {
                method: 'POST',
                body: formData
            });
            showToast(result.message || 'Configuración guardada.', 'success');
            await loadDashboardData();
        });
    });

    headerInput.addEventListener('change', () => {
        if (!headerInput.files.length) return;
        const url = URL.createObjectURL(headerInput.files[0]);
        document.getElementById('headerPreviewImage').src = url;
    });
}

function syncConfigForm() {
    if (!state.config) return;

    const config = state.config;
    document.getElementById('subjectNameInput').value = config.subjectName || '';
    document.getElementById('teachersInput').value = config.teachers || '';
    document.getElementById('examDateInput').value = config.examDate || '';
    document.getElementById('pageCountInput').value = config.pageCount || 1;

    document.getElementById('topicsPerGroupInput').value = config.topicsPerGroup || 1;
    document.getElementById('allowRepetitionInput').checked = !!config.allowTopicRepetition;

    document.getElementById('previewSubject').textContent = config.subjectName || '-';
    document.getElementById('previewTeachers').textContent = config.teachers || '-';
    document.getElementById('previewDate').textContent = config.examDate || '-';
    document.getElementById('previewPages').textContent = config.pageCount || '-';

    if (config.headerImageUrl) {
        document.getElementById('headerPreviewImage').src = config.headerImageUrl;
    }
}

function wirePdfsDashboard() {
    document.getElementById('generateAllPdfsBtn').addEventListener('click', async () => {
        if (!state.groups.length) return showError('No hay grupos para generar PDFs.');

        for (const group of state.groups) {
            triggerPdfDownload(group);
            addPdfHistory(group.name);
            await wait(450);
        }

        showToast('Se disparó la generación de PDFs para todos los grupos.', 'success');
    });
}

function renderPdfGroups() {
    const container = document.getElementById('pdfGroupsList');
    if (!state.groups.length) {
        container.innerHTML = '<div class="text-muted">Generá grupos para habilitar los PDFs.</div>';
        return;
    }

    container.innerHTML = state.groups.map((group) => `
      <div class="d-flex justify-content-between align-items-center border rounded p-2 mb-2">
        <div>
            <strong>${escapeHtml(group.name)}</strong>
            <div class="small text-muted">Integrantes: ${(group.members || []).length}</div>
        </div>
        <button class="btn btn-sm btn-outline-primary" data-pdf-group="${group.id}">
          <i class="bi bi-download"></i> PDF individual
        </button>
      </div>
    `).join('');

    container.querySelectorAll('[data-pdf-group]').forEach((button) => {
        button.addEventListener('click', () => {
            const groupId = Number(button.dataset.pdfGroup);
            const group = state.groups.find((g) => g.id === groupId);
            if (!group) return;
            triggerPdfDownload(group);
            addPdfHistory(group.name);
        });
    });
}

function triggerPdfDownload(group) {
    const anchor = document.createElement('a');
    anchor.href = `/api/pdf/group/${group.id}`;
    anchor.target = '_blank';
    anchor.rel = 'noopener';
    anchor.click();
}

function addPdfHistory(groupName) {
    const now = new Date().toLocaleString('es-AR');
    const history = JSON.parse(localStorage.getItem('pdfHistory') || '[]');
    history.unshift({ groupName, date: now });
    localStorage.setItem('pdfHistory', JSON.stringify(history.slice(0, 30)));
    restorePdfHistory();
}

function restorePdfHistory() {
    const list = document.getElementById('pdfHistoryList');
    const history = JSON.parse(localStorage.getItem('pdfHistory') || '[]');

    if (!history.length) {
        list.innerHTML = '<li class="list-group-item text-muted">Sin eventos registrados.</li>';
        return;
    }

    list.innerHTML = history.map((item) => `
      <li class="list-group-item d-flex justify-content-between">
        <span>${escapeHtml(item.groupName)}</span>
        <small class="text-muted">${escapeHtml(item.date)}</small>
      </li>
    `).join('');
}

function wireRouletteControls() {
    document.getElementById('rouletteSkipBtn').addEventListener('click', () => {
        state.rouletteSkipRequested = true;
    });

    document.getElementById('rouletteSoundBtn').addEventListener('click', (event) => {
        state.rouletteSoundEnabled = !state.rouletteSoundEnabled;
        event.target.textContent = state.rouletteSoundEnabled ? '🔊 Sonido' : '🔇 Silencio';

        if (!state.rouletteSoundEnabled) {
            stopRouletteAudio();
        } else {
            applyRouletteVolume();
        }
    });

    const volumeInput = document.getElementById('rouletteVolumeControl');
    if (volumeInput) {
        volumeInput.value = String(Math.round(rouletteAudio.volume * 100));
        volumeInput.addEventListener('input', () => {
            rouletteAudio.volume = Math.max(0, Math.min(1, Number(volumeInput.value) / 100));
            applyRouletteVolume();
        });
    }
}

async function runRouletteSequence({ title, items, rounds, statusPrefix, emojiSet = ROULETTE_GROUP_EMOJIS }) {
    const pool = (items || []).filter(Boolean);
    if (pool.length < 1) return;

    const overlay = document.getElementById('rouletteOverlay');
    const titleNode = document.getElementById('rouletteTitle');
    const wheel = document.getElementById('rouletteWheel');
    const statusNode = document.getElementById('rouletteStatus');

    state.rouletteSkipRequested = false;
    titleNode.textContent = title;
    overlay.classList.remove('d-none');

    renderRouletteItems(pool, emojiSet);

    if (state.rouletteSoundEnabled) {
        await playRouletteMusic();
    }

    const winners = [];
    let cumulativeRotation = 0;

    for (let round = 1; round <= rounds; round++) {
        if (state.rouletteSkipRequested) break;

        statusNode.textContent = `${statusPrefix} ${round}...`;
        const winner = pool[Math.floor(Math.random() * pool.length)];
        winners.push(winner);

        const spin = 2160 + Math.floor(Math.random() * 1800);
        cumulativeRotation += spin;
        wheel.style.transform = `rotate(${cumulativeRotation}deg)`;

        await wait(3900);
        highlightRouletteWinner(winner);
        explodeConfetti();
        if (state.rouletteSoundEnabled) {
            playWinSound();
        }
        await wait(700);
    }

    stopRouletteAudio();

    statusNode.textContent = winners.length
        ? `🎉 Seleccionado: ${winners[winners.length - 1]}`
        : 'Animación omitida';

    await wait(650);
    overlay.classList.add('d-none');
}

function renderRouletteItems(items, emojiSet = ROULETTE_GROUP_EMOJIS) {
    const wheel = document.getElementById('rouletteWheel');
    wheel.innerHTML = '';
    wheel.style.transform = 'rotate(0deg)';

    const anglePerItem = 360 / items.length;
    const gradientStops = items.map((_, index) => {
        const color = ROULETTE_COLORS[index % ROULETTE_COLORS.length];
        const start = (anglePerItem * index).toFixed(2);
        const end = (anglePerItem * (index + 1)).toFixed(2);
        return `${color} ${start}deg ${end}deg`;
    });

    wheel.style.background = `conic-gradient(${gradientStops.join(', ')})`;

    items.forEach((label, idx) => {
        const node = document.createElement('div');
        node.className = 'roulette-item';
        node.dataset.label = label;

        const rotation = idx * anglePerItem;
        const emoji = emojiSet[idx % emojiSet.length];

        node.style.transform = `translate(-50%, -50%) rotate(${rotation}deg) translateY(-170px) rotate(${-rotation}deg)`;
        node.innerHTML = `<span class="roulette-emoji">${emoji}</span><span class="roulette-label">${escapeHtml(label)}</span>`;
        wheel.appendChild(node);
    });
}

function highlightRouletteWinner(winner) {
    document.querySelectorAll('.roulette-item').forEach((node) => {
        node.classList.toggle('selected', node.dataset.label === winner);
    });
}

function explodeConfetti() {
    const overlay = document.getElementById('rouletteOverlay');
    for (let i = 0; i < 28; i++) {
        const confetti = document.createElement('span');
        confetti.className = 'confetti';
        confetti.style.left = `${30 + Math.random() * 40}%`;
        confetti.style.top = `${40 + Math.random() * 12}%`;
        confetti.style.background = ROULETTE_COLORS[Math.floor(Math.random() * ROULETTE_COLORS.length)];
        confetti.style.animationDelay = `${Math.random() * 100}ms`;
        overlay.appendChild(confetti);
        setTimeout(() => confetti.remove(), 980);
    }

    playConfettiSound();
}

async function playRouletteMusic() {
    try {
        if (!rouletteAudio.bgm) {
            rouletteAudio.bgm = new Audio('/sounds/roulette-music.mp3');
            rouletteAudio.bgm.loop = true;
        }
        applyRouletteVolume();
        rouletteAudio.bgm.currentTime = 0;
        await rouletteAudio.bgm.play();
    } catch (error) {
        // audio opcional
    }
}

function playWinSound() {
    if (!state.rouletteSoundEnabled) return;
    try {
        if (!rouletteAudio.win) {
            rouletteAudio.win = new Audio('/sounds/win-sound.mp3');
        }
        rouletteAudio.win.currentTime = 0;
        rouletteAudio.win.volume = rouletteAudio.volume;
        rouletteAudio.win.play();
    } catch (error) {
        // audio opcional
    }
}

function playConfettiSound() {
    if (!state.rouletteSoundEnabled) return;
    try {
        if (!rouletteAudio.confetti) {
            rouletteAudio.confetti = new Audio('/sounds/confetti-pop.mp3');
        }
        rouletteAudio.confetti.currentTime = 0;
        rouletteAudio.confetti.volume = Math.min(1, rouletteAudio.volume + 0.1);
        rouletteAudio.confetti.play();
    } catch (error) {
        // audio opcional
    }
}

function stopRouletteAudio() {
    if (rouletteAudio.bgm) {
        rouletteAudio.bgm.pause();
        rouletteAudio.bgm.currentTime = 0;
    }
}

function applyRouletteVolume() {
    if (rouletteAudio.bgm) {
        rouletteAudio.bgm.volume = rouletteAudio.volume;
    }
    if (rouletteAudio.win) {
        rouletteAudio.win.volume = rouletteAudio.volume;
    }
    if (rouletteAudio.confetti) {
        rouletteAudio.confetti.volume = Math.min(1, rouletteAudio.volume + 0.1);
    }
}

async function withLoading(buttonId, fn) {
    const button = document.getElementById(buttonId);
    const original = button ? button.innerHTML : null;

    if (button) {
        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Procesando...';
    }

    try {
        clearError();
        return await fn();
    } finally {
        if (button) {
            button.disabled = false;
            button.innerHTML = original;
        }
    }
}

async function apiFetch(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        let message = `Error ${response.status}`;
        try {
            const errorData = await response.json();
            message = errorData.message || message;
        } catch (ignored) {
            // respuesta no json
        }
        throw new Error(message);
    }

    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) {
        return response.json();
    }
    return null;
}

function showToast(message, type = 'primary') {
    const container = document.getElementById('toastContainer');
    const toastId = `toast-${Date.now()}`;

    const node = document.createElement('div');
    node.className = 'toast align-items-center text-bg-' + type + ' border-0';
    node.id = toastId;
    node.setAttribute('role', 'alert');
    node.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${escapeHtml(message)}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
      </div>
    `;

    container.appendChild(node);
    const toast = new bootstrap.Toast(node, { delay: 2600 });
    toast.show();
    node.addEventListener('hidden.bs.toast', () => node.remove());
}

function showError(message) {
    const alert = document.getElementById('globalValidationAlert');
    alert.textContent = message;
    alert.classList.remove('d-none');
    alert.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function clearError() {
    const alert = document.getElementById('globalValidationAlert');
    alert.textContent = '';
    alert.classList.add('d-none');
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function wait(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}