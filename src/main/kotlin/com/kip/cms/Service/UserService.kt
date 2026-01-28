@service
class UserService(
    private val userRepository: UserRepository,
    private val PasswordEncoder: PasswordEncoder
){
    fun createUser(user: User): User{
        validateUniqueUsername(user.username)
        validateUniqueUserEmail(user.email)

        val hashedPassword = passwordEncorder.encode(user.password)
        val userWithHashedPassword = user.copy(password = hashedPassword)

        return userRepository.save(userWithHashedPassword)
    }   

    fun findById(id long):User?{
        return userRepository.findById(user).orElse(null)
    }
    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }  
}