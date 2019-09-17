package ch.jvi.budgetmanager.backend.core.message

import ch.jvi.budgetmanager.backend.api.message.Message
import java.math.BigDecimal

data class CreateAccountMessage(val balance: BigDecimal, val name: String) : Message