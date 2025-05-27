// Quick test to verify credentials
fun main() {
    val password = "Password123!"
    
    println("Password: $password")
    println("Length: ${password.length}")
    println("Has uppercase: ${password.any { it.isUpperCase() }}")
    println("Has lowercase: ${password.any { it.isLowerCase() }}")
    println("Has digit: ${password.any { it.isDigit() }}")
    
    // Test each character
    password.forEachIndexed { index, char ->
        println("[$index] '$char' - Upper: ${char.isUpperCase()}, Lower: ${char.isLowerCase()}, Digit: ${char.isDigit()}")
    }
}
