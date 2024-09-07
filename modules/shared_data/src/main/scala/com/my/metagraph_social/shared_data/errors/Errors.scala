package com.my.metagraph_social.shared_data.errors

import cats.syntax.all._
import org.tessellation.currency.dataApplication.DataApplicationValidationError
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr

object Errors {
  type DataApplicationValidationType = DataApplicationValidationErrorOr[Unit]

  val valid: DataApplicationValidationType = ().validNec[DataApplicationValidationError]

  implicit class DataApplicationValidationTypeOps[E <: DataApplicationValidationError](err: E) {
    def invalid: DataApplicationValidationType = err.invalidNec[Unit]

    def unlessA(cond: Boolean): DataApplicationValidationType = if (cond) valid else invalid

    def whenA(cond: Boolean): DataApplicationValidationType = if (cond) invalid else valid
  }

  // Existing Errors
  case object TooLargePost extends DataApplicationValidationError {
    val message = "Post greater than 200 chars"
  }

  case object PostAlreadyExists extends DataApplicationValidationError {
    val message = "Post already exists"
  }

  case object PostNotExists extends DataApplicationValidationError {
    val message = "Post does not exist"
  }

  case object SubscriptionUserDoesNotExists extends DataApplicationValidationError {
    val message = "Subscription user does not exist"
  }

  case object UserAlreadySubscribed extends DataApplicationValidationError {
    val message = "User already subscribed"
  }

  case object CannotSubscribeSelf extends DataApplicationValidationError {
    val message = "Cannot subscribe to self"
  }

  // Added Errors
  case object TooLargeComment extends DataApplicationValidationError {
    val message = "Comment greater than 200 chars"
  }

  case object ParentPostNotExists extends DataApplicationValidationError {
    val message = "Parent post does not exist"
  }

  case object CommentsNotDeletedWithPost extends DataApplicationValidationError {
    val message = "Comments were not deleted with the post"
  }

  case object CommentDoesNotExist extends DataApplicationValidationError {
    val message = "Comment does not exist"
  }
}
