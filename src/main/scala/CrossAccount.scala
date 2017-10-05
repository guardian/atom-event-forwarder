import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider

trait CrossAccount {
  implicit def assumeRoleCredentials:STSAssumeRoleSessionCredentialsProvider = {
    new com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider
      .Builder(sys.env.get("CROSS_ACCOUNT_ROLE_ARN").get, "atom-event-forwarder")
      .build()
  }
}
