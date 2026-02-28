package stradar.common

enum class ChangeType {
    THREAT,
    OPPORTUNITY
}

enum class ChangeCategory {
    BUSINESS,
    OPERATING_MODEL,
    CAPABILITIES,
    PEOPLE_KNOWLEDGE
}

enum class ChangeDistance {
    DETECTED,
    ASSESSING,
    ASSESSED,
    RESPONDING
}

enum class ChangeImpact {
    LOW,
    MEDIUM,
    HIGH
}

enum class ChangeRisk {
    HIGH,
    MEDIUM,
    LOW
}
