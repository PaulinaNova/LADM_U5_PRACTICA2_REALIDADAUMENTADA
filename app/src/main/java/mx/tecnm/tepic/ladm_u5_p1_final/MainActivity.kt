package mx.tecnm.tepic.ladm_u5_p1_final

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*

class MainActivity : AppCompatActivity() {

    var baseRemota = FirebaseFirestore.getInstance()
    var nombre = ""
    val storage = Firebase.storage
    val storageRef = storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nombre = intent.getStringExtra("idNombre").toString()
        baseRemota.collection("lugares")
            .whereEqualTo("nombre", nombre)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val horario = document.getString("horario")!!
                    val descripcion = document.getString("descripcion")!!
                    val ruta = document.getString("ruta")!!
                    val rutas: List<String> = ruta.split(",")
                    cargarImagen(rutas[0],rutas[1],rutas[2])
                    txtNombre.setText(nombre)
                    txtHora.setText(horario)
                    txtDescripcion.setText(descripcion)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,"ERROR: ${it.message!!}",Toast.LENGTH_LONG).show()
            }

    }
    fun cargarImagen(path:String,path2:String,path3:String){
        var correcto = storageRef.child(path)
        var correcto2 = storageRef.child(path2)
        var correcto3 = storageRef.child(path3)
        val ONE_MEGABYTE: Long = 1024 * 1024
        correcto.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            var bit1 = BitmapFactory.decodeByteArray(it,0,it.size)
            imagen1.setImageBitmap(bit1)
        }
        correcto2.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            var bit2 = BitmapFactory.decodeByteArray(it,0,it.size)
            imagen2.setImageBitmap(bit2)
        }
        correcto3.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            var bit3 = BitmapFactory.decodeByteArray(it,0,it.size)
            imagen3.setImageBitmap(bit3)
        }
    }
}