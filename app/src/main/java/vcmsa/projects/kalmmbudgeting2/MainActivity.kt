package vcmsa.projects.kalmmbudgeting2


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.kalmmbudgeting2.LogNewEntryActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // find the button by its ID
        val btnLogNewEntry: Button = findViewById(R.id.button)

        // set up the click listener
        btnLogNewEntry.setOnClickListener {
            // create an explicit Intent to start LogNewEntryActivity
            val intent = Intent(this, LogNewEntryActivity::class.java)
            startActivity(intent)
        }
    }
}

