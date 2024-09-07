package com.my.metagraph_social.shared_data.validations

import cats.effect.Async
import cats.syntax.all._
import com.my.metagraph_social.shared_data.Utils.getFirstAddressFromProofs
import com.my.metagraph_social.shared_data.errors.Errors.valid
import com.my.metagraph_social.shared_data.types.States.SocialCalculatedState
import com.my.metagraph_social.shared_data.types.Updates._
import com.my.metagraph_social.shared_data.validations.TypeValidators._
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationValidationErrorOr
import org.tessellation.json.JsonSerializer
import org.tessellation.security.SecurityProvider
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

object Validations {
  def createPostValidationL1(
    update: CreatePost,
  ): DataApplicationValidationErrorOr[Unit] = {
    validateIfPostContentIsGreaterThan200Chars(update.content)
  }

  def editPostValidationL1(
    update: EditPost,
  ): DataApplicationValidationErrorOr[Unit] = {
    validateIfPostContentIsGreaterThan200Chars(update.content)
  }

  def deletePostValidationL1(): DataApplicationValidationErrorOr[Unit] = valid

  def subscriptionValidationL1(): DataApplicationValidationErrorOr[Unit] = valid

  // comment validations

  def createCommentValidationL1(update: CreateComment): DataApplicationValidationErrorOr[Unit] =
    validateIfCommentContentIsGreaterThan200Chars(update.content)

  def deleteCommentValidationL1(): DataApplicationValidationErrorOr[Unit] = valid

  def createPostValidationL0[F[_] : Async : SecurityProvider : JsonSerializer](
    signedUpdate   : Signed[CreatePost],
    calculatedState: SocialCalculatedState
  ): F[DataApplicationValidationErrorOr[Unit]] = for {
      updateBytes <- JsonSerializer[F].serialize[SocialUpdate](signedUpdate.value)
      postId = Hash.fromBytes(updateBytes).toString
      userId <- getFirstAddressFromProofs(signedUpdate.proofs)
      l1Validations = createPostValidationL1(signedUpdate.value)
      postAlreadyExists = validateIfPostAlreadyExists(postId, userId, calculatedState)
    } yield l1Validations.productR(postAlreadyExists)

  def editPostValidationL0[F[_] : Async : SecurityProvider](
    signedUpdate   : Signed[EditPost],
    calculatedState: SocialCalculatedState
  ): F[DataApplicationValidationErrorOr[Unit]] = for {
    userId <- getFirstAddressFromProofs(signedUpdate.proofs)
    l1Validations = editPostValidationL1(signedUpdate.value)
    postNotExists = validateIfPostExists(signedUpdate.postId, userId, calculatedState)
  } yield l1Validations.productR(postNotExists)

  def deletePostValidationL0[F[_] : Async : SecurityProvider](
    signedUpdate   : Signed[DeletePost],
    calculatedState: SocialCalculatedState
  ): F[DataApplicationValidationErrorOr[Unit]] = for {
    userId <- getFirstAddressFromProofs(signedUpdate.proofs)
    l1Validations = deletePostValidationL1()
    postNotExists = validateIfPostExists(signedUpdate.postId, userId, calculatedState)
  } yield l1Validations.productR(postNotExists)

  def subscriptionValidationL0[F[_] : Async : SecurityProvider](
    signedUpdate   : Signed[Subscribe],
    calculatedState: SocialCalculatedState
  ): F[DataApplicationValidationErrorOr[Unit]] =
    for {
      userId <- getFirstAddressFromProofs(signedUpdate.proofs)
      l1Validations = subscriptionValidationL1()
      subscriptionUserExists = validateIfSubscriptionUserExists(signedUpdate.value, calculatedState)
      userAlreadySubscribed = validateIfUserAlreadySubscribed(signedUpdate.value, userId, calculatedState)
      userTryingToSubscribeSelf = validateIfUserIsSubscribingToSelf(signedUpdate.value, userId)
    } yield l1Validations
      .productR(subscriptionUserExists)
      .productR(userAlreadySubscribed)
      .productR(userTryingToSubscribeSelf)

  // Comment Feature

  def createCommentValidationL0[F[_] : Async : SecurityProvider : JsonSerializer](
    signedUpdate   : Signed[CreateComment],
    calculatedState: SocialCalculatedState
  ): F[DataApplicationValidationErrorOr[Unit]] = for {
    updateBytes <- JsonSerializer[F].serialize[SocialUpdate](signedUpdate.value)
    _ = Hash.fromBytes(updateBytes).toString // Removed unused commentId assignment
    userId <- getFirstAddressFromProofs(signedUpdate.proofs)
    l1Validations = createCommentValidationL1(signedUpdate.value)
    parentPostExists = validateIfParentPostExists(signedUpdate.value.postId, userId, calculatedState)
  } yield l1Validations.productR(parentPostExists)

  // L0 Validation for Deleting a Comment
  def deleteCommentValidationL0[F[_] : Async : SecurityProvider](
    signedUpdate   : Signed[DeleteComment],
    calculatedState: SocialCalculatedState
  ): F[DataApplicationValidationErrorOr[Unit]] = for {
    userId <- getFirstAddressFromProofs(signedUpdate.proofs)
    l1Validations = deleteCommentValidationL1()
    commentExists = validateIfCommentExists(signedUpdate.value.commentId, signedUpdate.value.postId, userId, calculatedState)
  } yield l1Validations.productR(commentExists)

}
