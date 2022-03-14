package implementation.database.utils

import slick.lifted.Rep

trait WithRepId { def id : Rep[Long] }
