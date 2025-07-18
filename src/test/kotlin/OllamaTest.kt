import de.jackBeBack.Ollama
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class OllamaTest {

    val ollama = Ollama(model = "granite3.3")

    @Test
    fun generate() {
        runBlocking {
            ollama.generate("Write me a kotlin script")
        }
    }

    @Test
    fun chat() {
    }

    @Test
    fun embedding() {
        runBlocking {
            ollama.embedding("test")
        }
    }

    @Test
    fun listModels() {
        runBlocking {
            ollama.listModels()
        }
    }

}