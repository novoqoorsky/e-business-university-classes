package models.user

object UserRoles extends Enumeration {
  type UserRole = Value
  val User: UserRoles.Value = Value(1)
  val Admin: UserRoles.Value = Value(2)
}
