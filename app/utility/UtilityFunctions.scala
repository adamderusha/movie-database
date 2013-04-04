package utility

import java.security.SecureRandom

object UtilityFunctions {
  private val random: SecureRandom = new SecureRandom
  def createRequestId(): String = {
    new java.math.BigInteger(130, random).toString(32)
  }
}
