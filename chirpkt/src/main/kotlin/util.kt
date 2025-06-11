import com.fasterxml.uuid.Generators
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import java.security.SecureRandom

internal val uuidGenerator = Generators.timeBasedEpochRandomGenerator()
internal val cryptoRand = SecureRandom.getInstanceStrong()

internal fun java.time.LocalDateTime.toKotlinInstant() =
    toKotlinLocalDateTime().toInstant(UtcOffset.ZERO)
