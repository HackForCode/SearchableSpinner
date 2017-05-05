package `in`.galaxyofandroid.spinnerdialog

/**
 * Created by mihail on 05.05.17
 * Copying this class everywhere. Should stop!! Also, unlike many other [Either]s, it is extremely symmetric
 */
sealed class Either<out L, out R>
class Left<out T>(val value: T) : Either<T, Nothing>()
class Right<out T>(val value: T) : Either<Nothing, T>()

inline fun <EL, ER, R> Either<EL, ER>.let(left: (EL)->R, right: (ER)->R): R = when (this) {
    is Left -> left(value)
    is Right -> right(value)
}

inline fun <EL, ER, RL, RR> Either<EL, ER>.map(left: (EL) -> RL, right: (ER) -> RR): Either<RL, RR> =
        when (this) {
            is Left -> Left(left(value))
            is Right -> Right(right(value))
        }