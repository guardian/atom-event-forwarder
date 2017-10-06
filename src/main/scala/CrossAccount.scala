import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider

/* this file defines a trait which can be mixed in to add an implicit function to return assumed role credentials */

trait CrossAccount {
  implicit def assumeRoleCredentials:STSAssumeRoleSessionCredentialsProvider = {
    new com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
      .Builder(sys.env.get("CROSS_ACCOUNT_ROLE_ARN").get, "atom-event-forwarder")
      .build()
  }
}
