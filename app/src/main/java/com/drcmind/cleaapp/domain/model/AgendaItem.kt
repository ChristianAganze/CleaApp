package com.drcmind.cleaapp.domain.model

enum class AgendaKind(val apiValue: String, val label: String) {
    TASK("task", "Tâche"),
    REMINDER("reminder", "Rappel");

    companion object {
        fun fromApi(value: String): AgendaKind = entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: TASK
    }
}

enum class AgendaItemCategory(val apiValue: String, val label: String) {
    WORK("work", "Travail"),
    FAMILY("family", "Famille"),
    SPIRITUALITY("spirituality", "Spiritualité"),
    PERSONAL("personal", "Vie personnelle");

    companion object {
        fun fromApi(value: String): AgendaItemCategory = entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: PERSONAL
    }
}

enum class AgendaItemStatus(val apiValue: String) {
    PENDING("pending"),
    DONE("done"),
    CANCELLED("cancelled");

    companion object {
        fun fromApi(value: String): AgendaItemStatus = entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: PENDING
    }
}

data class AgendaItem(
    val id: String,
    val kind: AgendaKind,
    val title: String,
    val notes: String?,
    val category: AgendaItemCategory,
    val dueAt: String?,
    val remindAt: String?,
    val status: AgendaItemStatus,
    val createdAt: String?,
    val updatedAt: String?
) {
    val isDone: Boolean get() = status == AgendaItemStatus.DONE
}
