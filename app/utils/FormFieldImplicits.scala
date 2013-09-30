package utils

import play.api.data.format.{Formats, Formatter}
import play.api.data.FormError
import java.math.BigDecimal

object FormFieldImplicits {

  /**
   * Default formatter for the `BigDecimal` type with no precision
   */
  implicit val bigDecimalFormat: Formatter[BigDecimal] = bigDecimalFormat(None)

  def bigDecimalFormat(precision: Option[(Int, Int)]): Formatter[BigDecimal] = new Formatter[BigDecimal] {

    override val format = Some(("format.real", Nil))

    def bind(key: String, data: Map[String, String]) = {
      Formats.stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception.allCatch[java.math.BigDecimal]
          .either {
          val bd = new java.math.BigDecimal(s)
          precision.map({
            case (p, s) =>
              if (bd.precision - bd.scale > p - s) {
                throw new java.lang.ArithmeticException("Invalid precision")
              }
              bd.setScale(s)
          }).getOrElse(bd)
        }
          .left.map { e =>
          Seq(
            precision match {
              case Some((p, s)) => FormError(key, "error.real.precision", Seq(p, s))
              case None => FormError(key, "error.real", Nil)
            }
          )
        }
      }
    }

    def unbind(key: String, value: java.math.BigDecimal) = Map(key -> precision.map({ p => value.setScale(p._2) }).getOrElse(value).toString)
  }
}
