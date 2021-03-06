import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

data class Color(
    val name: String,
    val a: Float,
    val r: Float,
    val g: Float,
    val b: Float
) {

    val colorsetText: String
        get() {
            return """
{
  "colors" : [
    {
      "color" : {
        "color-space" : "srgb",
        "components" : {
          "alpha" : "$a",
          "blue" : "$b",
          "green" : "$g",
          "red" : "$r"
        }
      },
      "idiom" : "universal"
    }
  ],
  "info" : {
    "author" : "xcode",
    "version" : 1
  }
}     
""".trimIndent()
        }

    companion object {
        private fun hexToColor(hex: String): Float {
            return hex.toInt(16).toFloat() / 255f
        }

        fun fromNameAndHex(name: String, hex: String): Color? {
            if (!hex.startsWith("#")) return null
            val hexColor = hex.substring(1)
            val a: Float
            val r: Float
            val g: Float
            val b: Float
            if (hexColor.length == 8) {
                a = hexToColor(hexColor.substring(0, 2))
                r = hexToColor(hexColor.substring(2, 4))
                g = hexToColor(hexColor.substring(4, 6))
                b = hexToColor(hexColor.substring(6, 8))
            } else {
                a = 1f
                r = hexToColor(hexColor.substring(0, 2))
                g = hexToColor(hexColor.substring(2, 4))
                b = hexToColor(hexColor.substring(4, 6))
            }
            return Color(name.firstLowerCased, a, r, g, b)
        }
    }
}

internal fun readColors(filePath: String): List<Color> {
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val document = builder.parse(File(filePath))
    val resource = document.documentElement
    val colors = resource.getElementsByTagName("color")

    val result = mutableListOf<Color>()
    for (i in 0 until colors.length) {
        val color = colors.item(i) as Element
        val name = color.getAttribute("name")
        val hex = color.textContent.trim()
        Color.fromNameAndHex(name, hex)?.let {
            result.add(it)
        }
    }
    return result
}

fun main(args: Array<String>) {
    val parser = ArgParser("colorset-gen")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input colors.xml").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output directory")
    parser.parse(args)

    val colors = readColors(input)
    colors.forEach {
        val dir = File("$output/${it.name}.colorset/")
        dir.mkdir()
        val file = File(dir, "Contents.json")
        file.writeText(it.colorsetText)
    }
}
