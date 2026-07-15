import re

with open('app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import android.app.Activity
"""

content = content.replace('import androidx.compose.animation.*', imports + '\nimport androidx.compose.animation.*')

# add the launcher
launcher_code = """
    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    viewModel.cloudSync.handleSignInResult(context, account)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }
"""

content = content.replace('val infiniteTransition = rememberInfiniteTransition', launcher_code + '\n    val infiniteTransition = rememberInfiniteTransition')

# change the button onClick
button_old = """                                viewModel.cloudSync.signInSilently()
                                showSyncModal = false"""
button_new = """                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                showSyncModal = false"""
content = content.replace(button_old, button_new)

with open('app/src/main/java/com/example/ui/screens/MainWeatherScreen.kt', 'w') as f:
    f.write(content)
