package com.ai.roboteacher.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.speech.ModelDownloadListener
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ai.roboteacher.NetworkUtils.OkHttpClientInstance
import com.ai.roboteacher.R
import com.ai.roboteacher.RetrofitBuilder.RetrofitInstanceBuilder
import com.ai.roboteacher.SampleTextView
import com.ai.roboteacher.StatusApi
import com.ai.roboteacher.StatusService
import com.ai.roboteacher.Utils
import com.github.barteksc.pdfviewer.PDFView
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class SampleActivity : AppCompatActivity() {

	private val RECORD_AUDIO_PERMISSION_CODE: Int = 1001
	private val SPEECH_CODE = 100
	var txtView:TextView?=null
	var btn:Button?=null
	var stringBuilder:StringBuilder?=null
	var bfReader:BufferedReader?=null
	var markWon:Markwon?=null
	var p:Pattern = Pattern.compile("[A-Za-z]")
	private var isCountDownStarted:Boolean = false
	private var countDownProgress: ProgressBar?=null
	private var countDownLayout:View?=null
	private var progressText:TextView?=null
	private var isChecked = false

	var tstr = " Here’s a fun table that shows the five most common colors and the objects that are usually that color. It’s great for practicing colors and matching pictures to words!\n" +
			"                                                                                                   \n" +
			"                                                                                                    | Color   | Common Object | Quick Fun Fact |\n" +
			"                                                                                                    |---------|---------------|----------------|\n" +
			"                                                                                                    | Red     | Apple         | Apples can be sweet or a little sour, just like a surprise on your taste buds! |\n" +
			"                                                                                                    | Blue    | Sky           | The sky is blue because it reflects the blue light from the Sun. |\n" +
			"                                                                                                    | Yellow  | Sunflower     | Sunflowers turn to follow the Sun all day long. |\n" +
			"                                                                                                    | Green   | Grass         | Grass is green because it loves the sun and helps grow food for animals and people. |\n" +
			"                                                                                                    | Purple  | Grape         | Grape skins are purple, and when they’re crushed they make yummy juice! |\n" +
			"                                                                                                   \n" +
			"                                                                                                    Feel free to use this table to play a color‑matching game with your friends or to practice drawing each object. Enjoy learning about colors!"


	var str:ByteArray =
	"""# Welcome to Markwon 🌟

	This is a sample **Markdown** file to _showcase_ various ~~features~~ supported by **Markwon** in Android.

	## List Examples

	- Item 1
	- Item 2
	- Nested item
	- Item 3

	## Numbered List

	1. First
	2. Second
	3. Third

	## Links and Images

	Here’s a [link to Google](https://www.google.com).

	![Alt text](https://via.placeholder.com/100)

	## Code

	Inline code: `val a = 10`

	Code block:
	```kotlin
	fun greet(name: String) {
		println("Hello, !")
	}""".toByteArray()

	var ss = """$"""

	var eqStr = "## \uD83D\uDCD8 Quadratic Equation\n" +
			"\n" +
			"Every quadratic equation of the form \$\$ax^2 + bx + c = 0\$\$ can be solved using the quadratic formula shown below:\n"+
			"\n" +
			"\$\$\n" +
			"x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}\n" +
			"\$\$\n" +
			"\n" +
			"This equation gives the **two possible roots** (real or complex) of any second-degree polynomial.  \n" +
			"The term inside the square root, \$\$b^2 - 4ac\$\$, is called the **discriminant**, which determines the nature of the roots.\n" +
			"\n" +
			"---\n" +
			"\n" +
			"## \uD83D\uDCD7 Area Under a Curve\n" +
			"\n" +
			"The area under a curve \$\$y = f(x)\$\$ from \$\$x = a\$\$ to \$\$x = b\$\$ is given by the definite integral:\n" +
			"\n" +
			"\$\$\n" +
			"A = \\int_{a}^{b} f(x)\\,dx\n" +
			"\$\$\n" +
			"\n" +
			"If \$\$f(x)\$\$ is positive in \$\$[a, b]\$\$, this integral represents the **total area** between the curve and the x-axis.\n" +
			"\n" +
			"---\n" +
			"\n" +
			"## \uD83D\uDCD9 Pythagoras Theorem\n" +
			"\n" +
			"For a right-angled triangle with sides \$\$a\$\$ and \$\$b\$\$, and hypotenuse \$\$c\$\$, the relationship is:\n" +
			"\n" +
			"\$\$\n" +
			"a^2 + b^2 = c^2\n" +
			"\$\$\n" +
			"\n" +
			"This means the square of the hypotenuse is equal to the **sum of the squares** of the other two sides."

    var pagewidth = 0
    val pageWidth = 842f
    var pageHeight = 0

    var startX = 50f
    var startY = 100f

    var pageNumber = 1
    val pdfDocument = PdfDocument()
    var page:PdfDocument.Page? = null
    var pageInfo:PdfDocument.PageInfo?=null
    var canvas:Canvas?= null

	var nStrBuilder:StringBuilder = StringBuilder()

	var s2 = """How to find the roots of a quadratic equation
A quadratic equation looks like this:

${'$'} ax^{2} + bx + c = 0 ${'$'}

Follow these steps:

Identify the coefficients

${'$'} a ${'$'} is the number in front of ${'$'} x^2 ${'$'}

${'$'} b ${'$'} is the number in front of ${'$'} x ${'$'}

${'$'} c ${'$'} is the constant term

Compute the discriminant

Calculate ${'$'} D = b^2 - 4ac ${'$'}

Use the quadratic formula

The roots are
${'$'} \frac{-b \pm \sqrt{D}}{2a} ${'$'}

If ${'$'} D > 0 ${'$'}: two different real roots

If ${'$'} D = 0 ${'$'}: one real root (both roots are the same)

If ${'$'} D < 0 ${'$'}: no real roots (the solutions are imaginary)

Plug the numbers in

Example: For ${'$'} 2x^2 + 5x - 3 = 0 ${'$'}

${'$'} a = 2,; b = 5,; c = -3 ${'$'}

${'$'} D = 5^2 - 4(2)(-3) = 25 + 24 = 49 ${'$'}

Roots:
${'$'} \frac{-5 \pm \sqrt{49}}{4} = \frac{-5 \pm 7}{4} ${'$'}
So the two roots are ${'$'} x = \frac{2}{4} = 0.5 ${'$'} and ${'$'} x = \frac{-12}{4} = -3 ${'$'}.

Check your answers

Substitute each root back into the original equation to make sure it equals zero."""

	var s3 = """To find the roots (the values of ${'$'}${'$'} x ${'$'}${'$'} that make the equation true) of a quadratic equation  

${'$'}${'$'}
ax^{2}+bx+c=0
${'$'}${'$'}  

follow these simple steps:

1. **Identify the coefficients**  
   - ${'$'}${'$'} a ${'$'}${'$'} is the coefficient of ${'$'}${'$'} x^{2} ${'$'}${'$'}  
   - ${'$'}${'$'} b ${'$'}${'$'} is the coefficient of ${'$'}${'$'} x ${'$'}${'$'}  
   - ${'$'}${'$'} c ${'$'}${'$'} is the constant term  

2. **Compute the discriminant**  
   ${'$'}${'$'} 
   D=b^{2}-4ac
   ${'$'}${'$'}  
   The discriminant tells you what kind of roots you have.  
   * If ${'$'}${'$'} D>0 ${'$'}${'$'} there are two different real roots.  
   * If ${'$'}${'$'} D=0 ${'$'}${'$'} there is exactly one real root (a repeated root).  
   * If ${'$'}${'$'} D<0 ${'$'}${'$'} the roots are complex (contain ${'$'}${'$'} i ${'$'}${'$'}).

3. **Use the quadratic formula**  
   ${'$'}${'$'} 
   x=\frac{-b\pm\sqrt{D}}{2a}
   ${'$'}${'$'}  
   The ${'$'}${'$'} \pm ${'$'}${'$'} means you do the calculation twice: once with the plus sign and once with the minus sign.

4. **Simplify the results**  
   Reduce fractions if possible and write the numbers in simplest form.

### Example  
Find the roots of ${'$'}${'$'} 2x^{2}+5x-3=0 ${'$'}${'$'}.

- Here ${'$'}${'$'} a=2,\; b=5,\; c=-3 ${'$'}${'$'}.  
- Discriminant:  
  ${'$'}${'$'} 
  D=5^{2}-4(2)(-3)=25+24=49
  ${'$'}${'$'}  
- Since ${'$'}${'$'} D=49>0 ${'$'}${'$'}, there are two real roots.  
- Apply the formula:  
  ${'$'}${'$'} 
  x=\frac{-5\pm\sqrt{49}}{2(2)}=\frac{-5\pm7}{4}
  ${'$'}${'$'}  
  * With ${'$'}${'$'} + ${'$'}${'$'}: ${'$'}${'$'} x=\frac{-5+7}{4}=\frac{2}{4}=\frac{1}{2} ${'$'}${'$'}  
  * With ${'$'}${'$'} - ${'$'}${'$'}: ${'$'}${'$'} x=\frac{-5-7}{4}=\frac{-12}{4}=-3 ${'$'}${'$'}

So the equation ${'$'}${'$'} 2x^{2}+5x-3=0 ${'$'}${'$'} has roots ${'$'}${'$'} x=\frac12 ${'$'}${'$'} and ${'$'}${'$'} x=-3 ${'$'}${'$'}.

If the discriminant were negative, you would get a square root of a negative number, which means the roots are complex numbers (they involve ${'$'}${'$'} i ${'$'}${'$'}).
"""

	var s4 = "Sure! Let’s learn how to find the roots of a quadratic equation.  \n" +
			"A quadratic equation looks like  \n" +
			"\n" +
			"$$ ax^2 + bx + c = 0 $$  \n" +
			"\n" +
			"where **a, b,** and **c** are numbers (and **a** is never zero).  \n" +
			"\n" +
			"**Step 1: Use the Quadratic Formula**  \n" +
			"\n" +
			"The two (or one) solutions are\n" +
			"\n" +
			"$$ x = \\frac{-b \\pm \\sqrt{\\,b^2 - 4ac\\,}}{\\,2a\\,} $$  \n" +
			"\n" +
			"The part under the square root, **\$b^2 - 4ac$**, is called the **discriminant**.  \n" +
			"“$\\\\pm$” means you do the calculation twice: once with “+” and once with “–”.\n" +
			"\n" +
			"**Step 2: What the discriminant tells you**\n" +
			"\n" +
			"| Discriminant | Number of real roots |\n" +
			"|--------------|----------------------|\n" +
			"| $> 0$ | Two different real roots |\n" +
			"| $= 0$ | Exactly one real root (a double root) |\n" +
			"| $< 0$ | No real roots (the roots are imaginary numbers) |\n" +
			"\n" +
			"**Step 3: Quick example**\n" +
			"\n" +
			"Find the roots of  \n" +
			"\n" +
			"$$ 2x^2 + 3x - 4 = 0 $$  \n" +
			"\n" +
			"1. Identify \$a = 2$, \$b = 3$, \$c = -4$.  \n" +
			"2. Compute the discriminant:  \n" +
			"\n" +
			"$$ b^2 - 4ac = 3^2 - 4(2)(-4) = 9 + 32 = 41 $$  \n" +
			"\n" +
			"3. Apply the formula:  \n" +
			"\n" +
			"$$ x = \\frac{-3 \\pm \\sqrt{41}}{4} $$  \n" +
			"\n" +
			"So the two roots are  \n" +
			"\n" +
			"$$ x = \\frac{-3 + \\sqrt{41}}{4} \\quad \\text{and} \\quad x = \\frac{-3 - \\sqrt{41}}{4} $$  \n" +
			"\n" +
			"**Step 4: Try it yourself**\n" +
			"\n" +
			"Pick any values for \$a$, \$b$, and \$c$ (make sure \$a$ ≠ 0) and use the steps above to find the roots.  \n" +
			"\n" +
			"Happy solving!  \n" +
			"\n" +
			"<sugq>How do I find the discriminant?</sugq>  \n" +
			"<sugq>Can I solve \$x^2 - 4x + 4 = 0$?</sugq>  \n" +
			"<sugq>What happens when the discriminant is negative?</sugq>"


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_sample)

		val pdfView:PDFView = findViewById(R.id.pdfView)

	   val path = Utils.generatePdfNew1(applicationContext,eqStr)

		startActivity(
			Intent(this@SampleActivity, PdfActivity::class.java)
				.putExtra("pdfFile", path))


//		var r = Regex("\\s*[` ]+\\s*]")
//
//		val txt: SampleTextView = findViewById(R.id.sample_txt)
//		Log.d("abcde", "Data:$s4")
//
//		txt.post {
//
//			txt.setTextttt(s4)
//			//s.setText(intent.getStringExtra("data")!!)
//		}

//		if (r.find("```java")) {
//
//			Log.d(SampleActivity::class.java.name, "Match")
//		}

//		var s1 = "\\frac{-b \\pm \\sqrt{\\,b^{2}-4ac\\,}}{2a}"
//
//		var regex = Regex("\\\\frac\\{(.*?)\\}\\{(.*?)\\}")
//
//		var m = regex.find(s1)

		//Log.d(SampleActivity::class.java.name, "onCreate: "+m!!.groupValues[1])

//		val check = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
//
//		sendOrderedBroadcast(
//			check,
//			null,
//			object: BroadcastReceiver() {
//
//
//				override fun onReceive(context: Context?, intent: Intent?) {
//					val results = getResultExtras(true);
//					val langs =
//					results.getStringArrayList(
//						RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES
//					);
//
//					Log.d("STT", "Supported: " + langs);
//				}
//			},
//			null,
//			Activity.RESULT_OK,
//			null,
//			null
//		);




//		val editTextQuery = findViewById<TextView>(R.id.txt_view)
//
//		SpeechRecognizerInstance.getInstance(applicationContext,object :RecognitionListener{
//			override fun onReadyForSpeech(params: Bundle?) {
//
//			}
//
//			override fun onBeginningOfSpeech() {
//
//			}
//
//			override fun onRmsChanged(rmsdB: Float) {
//
//			}
//
//			override fun onBufferReceived(buffer: ByteArray?) {
//
//			}
//
//			override fun onEndOfSpeech() {
//
//			}
//
//			override fun onError(error: Int) {
//
//				Log.d(SampleActivity::class.java.name, "onError: "+error)
//
//			}
//
//			override fun onResults(results: Bundle?) {
//
////				results?.let {
////
////					val l = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
////					Log.d(SampleActivity::class.java.name, "onPartialResults: "+l?.firstOrNull())
////				}
//			}
//
//			override fun onPartialResults(partialResults: Bundle?) {
//
//				partialResults?.let {
//
//					val l = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//					val s:String? = l?.firstOrNull()?.trim()
//
//					nStrBuilder.clear()
//					nStrBuilder.append(s)
//
//					editTextQuery.text = nStrBuilder.toString()
//
//					Log.d(SampleActivity::class.java.name, "onPartialResults: "+l?.firstOrNull())
//
//				}
//			}
//
//			override fun onEvent(eventType: Int, params: Bundle?) {
//				TODO("Not yet implemented")
//			}
//
//
//		},10000)
//
//		val b = findViewById<Button>(R.id.btn_start)
//		//val view:ThinkingViewRoot = findViewById<ThinkingViewRoot>(R.id.t_root)
//		b.setOnClickListener {
//
//			SpeechRecognizerInstance.startSpeech()
//
//			//view.startAnims()
//
//
//
//
//		}

//		markWon = Markwon.builder(this)
//			.usePlugin(MarkwonInlineParserPlugin.create())
////            .usePlugin(ImagesPlugin.create(ImagesPlugin.))
//			.usePlugin(TablePlugin.create(this)) // <-- important
//			.usePlugin(MovementMethodPlugin.create(LinkMovementMethod.getInstance()))
//			.usePlugin((JLatexMathPlugin.create(16f,object: JLatexMathPlugin.BuilderConfigure{
//				override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
//
//					builder.inlinesEnabled(true)
//
//
//					builder.blocksEnabled(true);
//				}
//
//
//			})))
//			.build()
//
//		var txt:TextView = findViewById(R.id.txt_view)
//
//		var node = markWon!!.parse(s3)
//		var m1 = markWon!!.render(node)
//
//		txt.post {
//
//			markWon!!.setParsedMarkdown(txt,m1)
//
//
//		}





//		var fPath = Utils.generatePDF4(this,eqStr)
//
//		var intent = Intent(this@SampleActivity,PdfActivity::class.java)
//		intent.putExtra("pdfFile",fPath)
//		startActivity(intent)

		//var ssss = replaceDuplicateDollars(eqStr)

		//Log.d(SampleActivity::class.java.name, "onCreate: ${ssss}")



		//checkAudioPermissionAndStart()

//		var statusIntent: Intent = Intent(this, StatusService::class.java)
//		startService(statusIntent)
//
//		val statusServiceConnection:StatusServiceConnection = StatusServiceConnection()
//
//		//var statusIntent = Intent(this,StatusService::class.java)
//		bindService(statusIntent,statusServiceConnection!!, Context.BIND_AUTO_CREATE)
//
//		Handler().postDelayed({
//
//			unbindService(statusServiceConnection)
//
//		},6000)

		//Log.d(SampleActivity::class.java.name, "onCreate: ${replaceSingleDollars(s2)}")

//		val v:View = findViewById(R.id.sample_view)
//
//		var startX = 0f
//		var lastTouchX = 0f
//		var dX = 0f
//
//		v.setOnTouchListener(object:View.OnTouchListener{
//			override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//
//
//				when(event!!.action) {
//
//					MotionEvent.ACTION_DOWN->{
//
//						startX = event.rawX
//						dX = v!!.translationX
//
//
//						return true
//
//					}
//
//					MotionEvent.ACTION_MOVE->{
//
//						val diff = event.rawX - startX
//						v!!.translationX = dX + diff
//
//
//						return true
//					}
//
//					MotionEvent.ACTION_UP->{
//
//						//lastTouchX = event.x
//
//						//v!!.translationX = event.x
//
//						return true
//					}
//
//					else->{
//
//						return false
//
//					}
//
//
//
//
//				}
//			}
//
//
//		})

		//var s = SampleNotification(this,"jskdbsdb")

//        pagewidth = Utils.getScreenWidth(this)
////        val pageWidth = 842f
//        pageHeight = Utils.getScreenHeight(this)
//
//        pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
//        page = pdfDocument.startPage(pageInfo)
//        canvas = page!!.canvas
//
//		val lines = tstr.split("\n")
//
////        val regex:Regex = Regex("\\|(\\|.+\\|)+?\\|",RegexOption.DOT_MATCHES_ALL)
//
//		val regex:Regex = Regex("\\|(.*?)\\|",RegexOption.DOT_MATCHES_ALL)
//
//		for (l in lines) {
//
//			var m: MatchResult? = regex.find(l)
//
//			if (m!=null) {
//
//				//val rList = ArrayList<String>()
//
//                val rList = l.split("|")
//                    .map { it.trim() }
//                    .filter { it.isNotEmpty() }
//
////				while (m!=null) {
////
////					val s = m.value.trim().replace(Regex("\\|"),"")
////
////					//Log.d(SampleActivity::class.java.name, "onCreate: ${s.trim()}")
////					rList.add(s.trim())
////					m = m.next()
////				}
//
//                printPDF(rList)
//
//				Log.d(SampleActivity::class.java.name, "onCreate: ${rList.toString()}")
//
//			}
//
//		}
//
//        pdfDocument.finishPage(page)
//        val file = Utils.savePDF(this,pdfDocument)
//
//        var intent = Intent(this@SampleActivity,PdfActivity::class.java)
//        intent.putExtra("pdfFile",file)
//        startActivity(intent)











//        val click:Button = findViewById(R.id.click)
//
//        getBoardData1("usdhfkjahdjkashd")
//
//        click.setOnClickListener {
//
//            turnDeviceOff()
//
//
//        }




//
//        //var file = Utils.generatePDF(this,intent.getStringExtra("data")!!)
//        var file = Utils.generatePDF(this,eqStr)
//
//        var intent = Intent(this@SampleActivity,PdfActivity::class.java)
//        intent.putExtra("pdfFile",file)
//        startActivity(intent)


//        var smileImg:ImageView = findViewById(R.id.smile_img)
//
//        if (smileImg.drawable is AnimatedVectorDrawable) {
//
//            (smileImg.drawable as AnimatedVectorDrawable).start()
//        }



//        WhisperInstance.getInstance(this)
//        WhisperInstance.setDataListener(this)
//
//        WhisperInstance.startSpeech(this);



//        var roboRightEye:ImageView = findViewById(R.id.eye_right)
//
//        var roboRightDrawable:Drawable = roboRightEye.drawable
//
//        if (roboRightDrawable is Animatable) {
//
//            CoroutineScope(Dispatchers.Main).launch {
//
//                while (true) {
//
//
//                    roboRightDrawable.start()
//
//                    delay(400)
//
//                    roboRightDrawable.stop()
//
//                    delay(3000)
//                }
//
//
//            }
//
////            runEyeAnim(roboRightDrawable)
////            (roboRightDrawable as Animatable).start()
//        }
//
//        var roboLeftEye:ImageView = findViewById(R.id.eye_left)
//
//        var roboLeftDrawable:Drawable = roboLeftEye.drawable
//
//        if (roboLeftDrawable is Animatable) {
//
//            CoroutineScope(Dispatchers.Main).launch {
//
//                while (true) {
//
//                    roboLeftDrawable.start()
//
//                    delay(400)
//
//                    roboLeftDrawable.stop()
//
//                    delay(3000)
//                }
//
//
//            }
//        }
//
//        var smileImg:ImageView = findViewById(R.id.smile_img)
//        smileImg.setImageResource(R.drawable.animated_robo_smile_trim)
//
//        val vd:AnimatedVectorDrawable = smileImg.drawable as AnimatedVectorDrawable
//        vd.start()
////
//        smileImg.isSelected = false
//
//        val morphAnimator = ObjectAnimator.ofObject(vd.fi)

//        val drawable = smileImg.drawable
//
//        if (drawable is Animatable) {
//
//            (drawable as Animatable).start()
//
//
//        }



//        smileImg.setOnClickListener {
//
//            var morphAvd:AnimatedVectorDrawable?=null
//
//            if (!smileImg.isSelected) {
//
//                smileImg.isSelected = true
//                (smileImg.drawable as AnimatedVectorDrawable).stop()
//
//                morphAvd = ContextCompat.getDrawable(this,R.drawable.avd_pathmorph_smile_to_sad) as AnimatedVectorDrawable
//                smileImg.setImageDrawable(morphAvd)
//
//
//            } else {
//
//                smileImg.isSelected = false
//                (smileImg.drawable as AnimatedVectorDrawable).stop()
//
//                morphAvd = ContextCompat.getDrawable(this,R.drawable.avd_pathmorph_sad_to_smile) as AnimatedVectorDrawable
//                smileImg.setImageDrawable(morphAvd)
//
//            }
//
//            morphAvd!!.start()
//
//            morphAvd.registerAnimationCallback(object : Animatable2.AnimationCallback(){
//
//                override fun onAnimationStart(drawable: Drawable?) {
//                    //super.onAnimationStart(drawable)
//                }
//
//                override fun onAnimationEnd(drawable: Drawable?) {
//
//                    if (smileImg.isSelected) {
//
//                        smileImg.setImageResource(R.drawable.animated_robo_sad_trim)
//
//                        (smileImg.drawable as AnimatedVectorDrawable).start()
//
//                    } else {
//
//                        smileImg.setImageResource(R.drawable.animated_robo_smile_trim)
//
//                        (smileImg.drawable as AnimatedVectorDrawable).start()
//
//
//                    }
//
//
//
//
//                }
//            })
//
////            isChecked = !isChecked
////
////            val stateSet = intArrayOf(android.R.attr.state_checked * (if (isChecked) 1 else -1))
////            smileImg.setImageState(stateSet, true)
//
//
//        }

//        var btn:Button = findViewById(R.id.button_start)
//
//        btn.setOnClickListener {
//
//            if (ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
//                !=PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),RECORD_AUDIO_PERMISSION_CODE)
//
//            } else {
//
//                //initRecorderAndModel()
//
//                WhisperInstance.getInstance(this)
//
//                Handler().postDelayed({WhisperInstance.startSpeech(this)},3000)
//
//
//            }
//
//        }

//        countDownLayout = findViewById(R.id.count_down_layout)
//        countDownProgress = countDownLayout!!.findViewById(R.id.progressRing)
//        progressText = countDownLayout!!.findViewById(R.id.progress_text)
//
//        lifecycleScope.launch {
//
//            while (true) {
//
//                var sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
//                var date: Date? = sdf.parse("2025-07-28T10:51:00}")
//                val tmpStmp:Long? = date?.time
//
//
//                Log.d(ChatActivity::class.java.name, "FutureTimeStamp "+tmpStmp)
//
//                if (tmpStmp!!-System.currentTimeMillis()<=60000) {
//
//                    if (!isCountDownStarted) {
//
//                        isCountDownStarted = true
//
//                        //launch Countdown
//
//                        lifecycleScope.launch {
//
//                            initiateCountDown()
//
//                        }
//                    }
//
//                    break
//
//                }
//
////                        if (System.currentTimeMillis()>=tmpStmp!!) {
////
////                            //showEndDialog
////
////                            val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder()
////                            customAlertDialogBuilder.buildAlertDialog(this@ChatActivity
////                                ,"Hello!"
////                                ,"Class has ended"
////                                , R.drawable.panda).show()
////
////                            break
////
////                        } else if (tmpStmp-System.currentTimeMillis()<=60000) {
////
////                            if (!isCountDownStarted) {
////
////                                isCountDownStarted = true
////
////                                //launch Countdown
////
////                                lifecycleScope.launch {
////
////                                    initiateCountDown()
////
////                                }
////                            }
////
////                        }
//
//                delay(5000)
//
//
//            }
//
//        }

//         markWon = Markwon.create(this);

//        btn = findViewById<Button>(R.id.start_vr)
//        txtView = findViewById(R.id.text_view)

//        btn?.setOnClickListener {
//
//            btn?.isSelected = true
//
//
//
//                OkHttpClientInstance.getInstance(object :OkHttpClientInstance.ResultReceiver{
//                    override fun onResult(code: Int, response: Response) {
//
//                        txtView!!.text = response.body?.string()
//                    }
//
//                    override fun onError(code: Int, response: Response) {
//
//                    }
//
//
//                })
//
//                OkHttpClientInstance.get("classList.php")
//
////                getData().collect({
////
//////                    var node:Node = markWon!!.parse(it);
//////
//////                    var markDown = markWon!!.render(node)
//////
//////                    markWon!!.setParsedMarkdown(txtView!!,markDown)
////
//////                    txtView!!.setText()
////
////                    var matcher = p.matcher(it)
////
////                    if (matcher.find()) {
////
////                        Log.d(SampleActivity::class.java.name, it.substring(matcher.regionStart()
////                            ,matcher.regionEnd())
////                        )
////
////
////                    }
////
////                    while (matcher.find()) {
////
////                        //send for TextToSpeech
////
////
////                    }
////
////                    val node = markWon!!.parse(it)
////                    val renderedMarkdown = markWon!!.render(node)
////                    txtView!!.append(renderedMarkdown)
////                    txtView!!.append("\n\n")
////
//////                    if (matcher.matches()) {
//////
//////                        matcher.find()
//////
//////                    }
////
////                    //txtView!!.append(markWon!!.toMarkdown(it))
////
////
////                })
//
//
//
//
//            //checkAudioPermissionAndStart()
//
//
//
//        }


	}

	fun replaceDuplicateDollars(s:String):String {

		var sB = StringBuilder()

		var s1 = ""
		var sIndex = 0

		var index = s.indexOf("$",sIndex)

		while (index != -1) {

			if (index + 1 < s.length && s[index + 1] == '$') {
				// Already $$ → skip both dollars

				s1 = s.substring(sIndex,index+2)
				sB.append(s1)

				sIndex = index+2
				index = s.indexOf("$", sIndex)
			} else {
				// Replace only this single $
				s1 = s.substring(sIndex, index) + "$$"

				sB.append(s1)

				sIndex = index+1


				index = s.indexOf("$", sIndex) // skip the newly inserted $$
			}
		}

		return sB.toString()
	}



	fun replaceSingleDollars(s: String): String {
		val sb = StringBuilder()
		var i = 0

		while (i < s.length) {
			if (i + 1 < s.length && s[i] == '$' && s[i + 1] == '$') {
				// Copy $$ as-is
				sb.append("$$")
				i += 2
			} else if (s[i] == '$') {
				// Replace single $ with $$
				sb.append("$$")
				i++
			} else {
				sb.append(s[i])
				i++
			}
		}

		return sb.toString()
	}

	fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val padding = 10f
		val words = text.split(" ")
		val lines = mutableListOf<String>()
		var currentLine = StringBuilder()

		for (word in words) {
			val testLine = if (currentLine.isEmpty()) word else currentLine.toString() + " " + word
			if (paint.measureText(testLine) > maxWidth - 2 * padding) {
				lines.add(currentLine.toString())
				currentLine = StringBuilder(word)
			} else {
				currentLine = StringBuilder(testLine)
			}
		}
		if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
		return lines
	}


//
	private fun printPDF(lines:List<String>) {



    val pagewidth = Utils.getScreenWidth(this)
    val pageWidth = 842f
    val pageHeight = Utils.getScreenHeight(this)

    var xPosition = 295f
    var yPosition = 25f
//    var pageNumber = 1



	val paint = Paint().apply {
		color = Color.BLACK
		textSize = 14f
		style = Paint.Style.STROKE
	}

	val cellWidth = 180f   // fixed width for all columns
	val padding = 10f
	val lineSpacing = paint.fontSpacing

	val lineCounts = lines.map { wrapText(it, cellWidth, paint).size }
	val maxLines = lineCounts.maxOrNull() ?: 1
	val rowHeight = maxLines * lineSpacing + padding * 2

	var x = startX
	for (text in lines) {

        if (startY+rowHeight>pageHeight-25) {

            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pagewidth,pageHeight,pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page!!.canvas
            //yPosition = 25f

            x = startX
            startY = 100f

        }
		// Draw cell rectangle
		canvas!!.drawRect(x, startY, x + cellWidth, startY + rowHeight, paint)

		// Draw wrapped text
		val lines = wrapText(text, cellWidth, paint)
		paint.style = Paint.Style.FILL
		var textY = startY + padding + paint.textSize
		for (line in lines) {
			canvas!!.drawText(line, x + padding, textY, paint)
			textY += lineSpacing
		}
		paint.style = Paint.Style.STROKE
		x += cellWidth
	}

	startY+=rowHeight

//        for (row in listOf(headers) + rows) {
//            // Compute row height dynamically based on longest text
////            val lineCounts = row.map { wrapText(it, cellWidth, paint).size }
////            val maxLines = lineCounts.maxOrNull() ?: 1
////            val rowHeight = maxLines * lineSpacing + padding * 2
//
//
//        }
	}



	private fun turnDeviceOff() {

		var url:String = RetrofitInstanceBuilder.BASEURL.replace("8012","8020")
		url = url+RetrofitInstanceBuilder.shutDown

		OkHttpClientInstance.getInstance(object: OkHttpClientInstance.ResultReceiver{
			override fun onResult(code: Int, response: Response?) {

				Log.d("aaaa", "onResult: "+response!!.body!!.string())

//                runOnUiThread {
//
//                    isTurnedOff = true
//
//                    simulateShutdown(this@GeneralActivity)
//
//                }

			}

			override fun onError(code: Int, response: Response?) {
				Log.d("aaaa", "onResult: ${code} "+response!!.body!!.string())
			}

			override fun onException(error: String?) {
				Log.d("aaaa", "onResult: "+error)
			}


		})

		OkHttpClientInstance.post(url,null)

	}

	private fun getBoardData1(url: String) {

		val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
			override fun log(message: String) {
				Log.d("OkHttp", message)
			}
		})

		val okHttpClient = OkHttpClient.Builder()
			.addInterceptor(logging)
			.retryOnConnectionFailure(true)
			.connectionPool(ConnectionPool(5, 5, TimeUnit.SECONDS))
			.build()


		val retrofit = Retrofit.Builder()
			.baseUrl("http://192.168.1.41:8015/") // Make sure this ends with a slash
			.client(okHttpClient)
			.build()

		val statusApi: StatusApi = retrofit.create(StatusApi::class.java)

		CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				try {
					val response = statusApi.getStatus()

					//Log.d("StatusService", "Response: code=${response.code()}, success=${response.isSuccessful}")

					if (response.isSuccessful && response.code() == 200) {
						response.body()?.string()?.let {
							val statusJson = JSONObject(it)
							val status = statusJson.getString("status")
							//statusCallback?.invoke(status)
							Log.d("StatusService", "Status: $status")
						}
					} else {
						//statusCallback?.invoke("down")
						Log.w("StatusService", "Server responded with code ${response.code()}")
					}

				} catch (ex: IOException) {
					Log.e("StatusService", "Connection error", ex)
					//okHttpClient.connectionPool.evictAll()
				}

				delay(5000)
			}
		}




	}

//    private fun runEyeAnim(drawable: AnimatedVectorDrawable) {
//
//        var r:java.lang.Runnable = object:java.lang.Runnable{
//            override fun run() {
//
//
//            }
//
//
//        }
//
//
//
//
//
//
//
//        Handler().post {
//
//
//        }
//
//        drawable.start()
//
//
//
//
//
//
//
//
//    }

	private suspend fun initiateCountDown() {

		Log.d(ChatActivity::class.java.name, "launched")

		runOnUiThread {

			countDownLayout?.visibility = View.VISIBLE
			countDownProgress?.max = 60


		}

		for (i in 0..5) {

			runOnUiThread {

				countDownProgress?.progress = 60-i
				progressText?.text = countDownProgress?.progress.toString()

				if (i == 5) {

					var widthPixels = (resources.displayMetrics.widthPixels*0.70f).toInt()
					var heightPixels = resources.displayMetrics.heightPixels

					var dialog = Dialog(this)
					dialog.setContentView(R.layout.dialog_your_feedback)
					dialog.window?.setLayout(widthPixels,heightPixels)
					dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
					dialog.create()
					dialog.show()
				}

//                if (countDownProgress?.progress == 0) {
//
//                    val customAlertDialogBuilder:CustomAlertDialogBuilder = CustomAlertDialogBuilder()
//                    customAlertDialogBuilder.buildAlertDialog(this@ChatActivity
//                        ,"Hello!"
//                        ,"Class has ended"
//                        , R.drawable.panda).show()
//
//
//                }

			}

			delay(1000)




		}


	}

//	private fun initRecorderAndModel(modelFile:File) {
//
//		val isMultilingualModel: Boolean =
//			!(modelFile.getName().endsWith(WhisperInstance.ENGLISH_ONLY_MODEL_EXTENSION))
//		val vocabFileName: String =
//			if (isMultilingualModel) WhisperInstance.MULTILINGUAL_VOCAB_FILE else WhisperInstance.ENGLISH_ONLY_VOCAB_FILE
//		val vocabFile: File = File(WhisperInstance.ROOT_FILE_PATH, vocabFileName)
//
//		var mWhisper = Whisper(this)
//		mWhisper.loadModel(modelFile, vocabFile, isMultilingualModel)
//		mWhisper.setListener(object : WhisperListener {
//			override fun onUpdateReceived(message: String) {
//				Log.d(SampleActivity::class.java.name, "Update is received, Message: $message")
//
//				if (message == Whisper.MSG_PROCESSING) {
//
//				}
//
//				if (message == Whisper.MSG_PROCESSING_DONE) {
////                    handler.post(() -> tvStatus.setText(message));
//					// for testing
//
//				} else if (message == Whisper.MSG_FILE_NOT_FOUND) {
//
//				}
//			}
//
//			override fun onResultReceived(result: String) {
//
//
//				Log.d(SampleActivity::class.java.name, "Result: $result")
//
//			}
//
//			override fun onRmsReceived(result: Double) {
//				TODO("Not yet implemented")
//			}
//		})
//
//	}

	private fun getData():Flow<String> = flow<String> {

		var b = ByteArray(1024)

		var reader = BufferedReader(InputStreamReader(str.inputStream()))
//        var ipStream =

//        while (ipStream.read(b,0,b.size)!=-1) {
//
//            emit(String(b))
//        }

		var s:String = reader.readLine()

		while (s!=null) {

			emit(s)

			s = reader.readLine()
		}


	}

	private fun getDataKtor(
		query: String? = "Please recite the poem 'What a bird thought'.",
		cls: Int = 6,
		subject: String? = "english"
	): Flow<String> = flow<String> {


		val json = JSONObject().apply {
			put("query_text", query)
			put("class_number", cls)
			put("subject", subject)
		}

		var httpClient = HttpClient(CIO) {
			install(ContentNegotiation) {
				json()
			}

			engine {
				requestTimeout = 0 // ✅ For streaming: disable total timeout
				endpoint {
					connectTimeout = 15_000
					socketTimeout = 60_000
				}
			}
		}

		httpClient.preparePost(RetrofitInstanceBuilder.BASEURL + RetrofitInstanceBuilder.STUDY) {

			contentType(Json)

			setBody(json.toString())
		}.execute {


			if (it.status.isSuccess()) {


				val channel: ByteReadChannel = it.bodyAsChannel()


				val buffer = ByteArray(1024)

				while (!channel.isClosedForRead) {



					try {

						var line = channel.readUTF8Line()
						emit(line!!)


//                        val bytesRead: Int = channel.readAvailable(buffer, 0, buffer.size)
//
//                        emit(buffer.copyOf(bytesRead))

					} catch (ex: Exception) {

//                        if (isAnim) {
//
//                            dataSet.get(index).clear()
//                            dataSet.get(index).append("Server Error : ${ex.message}")
//                            dataSet.get(index).setSpan(
//                                ForegroundColorSpan(Color.RED)
//                                ,0,dataSet.get(index).length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//                            isAnim = false
//                            chatEditText.isSelected = false
//
//
//                            chatRecyclerView.post {
//
//                                chatAdapter.notifyItemChanged(index)
//                            }
//
//
//                        } else {
//
//                            chatEditText.isSelected = false
//
//
//                        }

//                        if (dataSet.get(index).length == 0) {
//
//                            dataSet.get(index).clear()
//                            dataSet.get(index).append("Server Error : ${ex.message}")
//                            dataSet.get(index).setSpan(ForegroundColorSpan(Color.RED)
//                                ,0,dataSet.get(index).length,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
//                            isAnim = false
//
//
//                            chatRecyclerView.post {
//
//                                chatAdapter.notifyItemChanged(index)
//                            }
//
//
//                        }


//                        runOnUiThread {
//
//                            Toast.makeText(
//                                this@ChatActivity, "Server Error : ${it.status.value} ${ex.message}", Toast.LENGTH_SHORT
//                            ).show()
//
//                        }



					}

				}

				//chatEditText.isSelected = false


			}


		}

		httpClient.close()

	}.flowOn(Dispatchers.IO)

//    private fun getDataKtorr(
//        query: String? = "Please recite the poem 'What a bird thought'.",
//        cls: Int = 6,
//        subject: String? = "english"
//    ): Flow<String?> = flow {
//
//
//
//        var buffer:ByteArray = ByteArray(1024)
//
//
//
//        while (strBytes.)
//
//
//
//
//
////        val json = JSONObject().apply {
////            put("query_text", query)
////            put("class_number", cls)
////            put("subject", subject)
////        }
////
////        val httpClient = HttpClient(CIO) {
////            install(ContentNegotiation) {
////                json()
////            }
////
////            engine {
////                requestTimeout = 0 // ✅ For streaming: disable total timeout
////                endpoint {
////                    connectTimeout = 15_000
////                    socketTimeout = 60_000
////                }
////            }
////        }
//
////        httpClient.preparePost(RetrofitInstanceBuilder.BASEURL + RetrofitInstanceBuilder.STUDY) {
////
////            contentType(Json)
////
////            setBody(json.toString())
////        }.execute {
////
////            val channel: ByteReadChannel = it.bodyAsChannel()
////            val buffer = ByteArray(1024)
////
////            while (!channel.isClosedForRead) {
////
////                emit(channel.readUTF8Line()?:"")
////
////
////
////
//////                try {
//////
//////                    val bytesRead:Int = channel.readAvailable(buffer,0,buffer.size)
//////
//////                    emit(buffer.copyOf(bytesRead))
//////
//////                } catch (ex:Exception) {
//////
//////
//////                }
////
////
////
////
////                //emit(channel.read {  })
////
////
////            }
//
////            while (!channel.isClosedForRead) {
////                val buffer:ByteBuffer = ByteBuffer(DEFAULT_BUFFER_SIZE)
////                channel.readAvailable(buffer)
////                val trimmed = buffer.dropLastWhile { it == 0.toByte() }.toByteArray()
////                emit(String(trimmed))
////            }
//
//
//
//
////        val channel = response.bodyAsChannel()
////
////        while (!channel.isClosedForRead) {
////            val line = channel.readUTF8Line()
////            if (line != null) {
////                emit(line + "\n") // ✅ emit as Flow<String>
////            }
////        }
//
//        //httpClient.close()
//
//    }.flowOn(Dispatchers.IO)

	private fun checkAudioPermissionAndStart() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
			!= PackageManager.PERMISSION_GRANTED
		) {
			ActivityCompat.requestPermissions(
				this,
				arrayOf<String>(Manifest.permission.RECORD_AUDIO),
				RECORD_AUDIO_PERMISSION_CODE
			)
		} else {
			startSpeechRecognizer() // Your method to launch recognizer
		}
	}

	private lateinit var speech: SpeechRecognizer
	private lateinit var recognizerIntent: Intent

	private var currentRms = -100f
	private var maxRmsDuringSpeech = -100f

	private var isSpeaking = false
	private var silenceTimer: CountDownTimer? = null

	private val RMS_THRESHOLD = 1.5f     // reject background & low-pitch people
	private val SPEAKING_START_THRESHOLD = 6.0f
	private val SPEAKING_STOP_DELAY = 600L

	private fun startSpeechRecognizer() {

		val recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext)

		val rintent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
			putExtra(
				RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
			)

			// Correct language
			putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
			putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
			putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "en-US")
			putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

			putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20000)
			putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
			putExtra(
				RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
				3000
			)

			//putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
			//putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
			putExtra(
				RecognizerIntent.EXTRA_AUDIO_SOURCE,
				MediaRecorder.AudioSource.VOICE_RECOGNITION
			)
//                }



//		if (SpeechRecognizer.isRecognitionAvailable(this@SampleActivity)) {
//
//			Log.d(SelectClassActivity::class.java.name, "startSpeechRecognizerAvailable")
//
//			val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
//
//			var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//			intent.putExtra(
//				RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//
//			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//			//intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
//			//intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak Now...")
//
//			speechRecognizer.setRecognitionListener(object : RecognitionListener {
//				override fun onReadyForSpeech(params: Bundle?) {
//
//					Toast.makeText(this@SampleActivity
//						,"Speak Now..."
//						, Toast.LENGTH_SHORT).show()
//
//				}
//
//				override fun onBeginningOfSpeech() {
//
//				}
//
//				override fun onRmsChanged(rmsdB: Float) {
//
//				}
//
//				override fun onBufferReceived(buffer: ByteArray?) {
//
//				}
//
//				override fun onEndOfSpeech() {
//
//					btn?.isSelected = false
//
//				}
//
//				override fun onError(error: Int) {
//
//					Log.d(SelectClassActivity::class.java.name, "onError: ${error}")
//
//					if (error == 12 || error == 7 || error == 13) {
//
//						triggerModelDownload(speechRecognizer,intent)
//
//						//speechRecognizer.triggerModelDownload(intent)
//
//
//					}
//					btn?.isSelected = false
//
//
//				}
//
//				override fun onResults(results: Bundle?) {
//
//					var speechResultList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//					Log.d(SelectClassActivity::class.java.name, "Results: ${speechResultList?.get(0)}")
//					//Toast.makeText(this@SampleActivity,speechResultList?.get(0),Toast.LENGTH_SHORT).show()
//					txtView?.setText(speechResultList?.get(0))
//					//speechResultList = null
//					btn?.isSelected = false
//
//				}
//
//				override fun onPartialResults(partialResults: Bundle?) {
//
//				}
//
//				override fun onEvent(eventType: Int, params: Bundle?) {
//
//				}
//
//
//			})
//
//			speechRecognizer.startListening(intent)
//
//
//		}


		}

//		private var currentRms = -100f
//		private var maxRmsDuringSpeech = -100f
//
//		private var isSpeaking = false
//		private var silenceTimer: CountDownTimer? = null
//
//		private val RMS_THRESHOLD = 0.5f     // reject background & low-pitch people
//		private val SPEAKING_START_THRESHOLD = 2.8f
//		private val SPEAKING_STOP_DELAY = 600L

		recognizer.setRecognitionListener(object : RecognitionListener {

			override fun onReadyForSpeech(params: Bundle?) {
				maxRmsDuringSpeech = -1.7f
				isSpeaking = false
			}

			override fun onRmsChanged(rmsdB: Float) {
				currentRms = rmsdB

				Log.d(SampleActivity::class.java.name, "onRmsChanged: "+rmsdB)

//				if (rmsdB > maxRmsDuringSpeech) {
//					maxRmsDuringSpeech = rmsdB
//				}

				// Start "speaking mode"
				if (!isSpeaking && rmsdB > SPEAKING_START_THRESHOLD) {
					isSpeaking = true
					//silenceTimer?.cancel()
				}

				// Stop speaking after silence
				if (isSpeaking && rmsdB < SPEAKING_START_THRESHOLD) {

					isSpeaking = false
//					silenceTimer?.cancel()
//					silenceTimer =
//						object : CountDownTimer(SPEAKING_STOP_DELAY, SPEAKING_STOP_DELAY) {
//							override fun onTick(millisUntilFinished: Long) {}
//							override fun onFinish() {
//								isSpeaking = false
//							}
//						}.start()
				}
			}

			override fun onPartialResults(results: Bundle?) {
				if (!isSpeaking) return
//				if (maxRmsDuringSpeech < 0.1f) return   // 100% reject distant or background speakers

				val textList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
				val text = textList?.getOrNull(0) ?: return

				Log.d(SampleActivity::class.java.name, "onPartialResults: "+text)

//					if (isGarbage(text)) return
//
//					onValidSpeech(text)
			}

			override fun onResults(results: Bundle?) {
//					val textList = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//					val text = textList?.getOrNull(0) ?: return
//
//					if (isGarbage(text)) return
//					if (maxRmsDuringSpeech < -35f) return
//
//					onValidSpeech(text)
//					restartRecognizer()
			}

			override fun onError(error: Int) {

				Log.d(SampleActivity::class.java.name, "onError: ${error}")
				//restartRecognizer()
			}

			override fun onBeginningOfSpeech() {}
			override fun onEndOfSpeech() {}
			override fun onBufferReceived(buffer: ByteArray?) {}
			override fun onEvent(eventType: Int, params: Bundle?) {}
		})

		recognizer!!.startListening(rintent)
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {


		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
			if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//startSpeechRecognizer()

				//WhisperInstance.getInstance(this)
				//WhisperInstance.startSpeech(this)
			} else {
				Toast.makeText(
					this,
					"Microphone permission is required for speech recognition",
					Toast.LENGTH_SHORT
				).show()
			}
		}
	}

	private fun triggerModelDownload(speechRecognizer:SpeechRecognizer,intent:Intent) {

		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {

			speechRecognizer.triggerModelDownload(intent,Executors.newSingleThreadExecutor()
				,

				object:ModelDownloadListener{
					override fun onProgress(completedPercent: Int) {

						Log.d(SampleActivity::class.java.name, "onProgress: ${completedPercent}")

					}

					override fun onSuccess() {

						Log.d(SampleActivity::class.java.name, "onSuccess: Model Downloaded")

					}

					override fun onScheduled() {

					}

					override fun onError(error: Int) {

						Log.d(SampleActivity::class.java.name, "onError ${error}")

					}


				})




		}


	}

	var statusBinder:StatusService.StatusBinder?=null

	private inner class StatusServiceConnection: ServiceConnection {

		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

			//isBound = true

			statusBinder = service as StatusService.StatusBinder
			statusBinder!!.setStatusCallback {

				Log.d(SampleActivity::class.java.name, "onServiceConnected: "+it)

//				if(it.equals("down")) {
//
//					status = it
//
//
//					if (dataReceiver!=null) {
//
//						dataReceiver!!.isStopped = true
//					}
//
//					if (!isTurnedOff) {
//
//
//						if (!isServiceAlert) {
//
//							isServiceAlert = true
//
//							showServiceAlert()
//
//
//						}
//
//						runOnUiThread {
//
//							expView!!.setExpression(RoboExpressionView.Expressions.NEUTRAL)
//						}
//
//						if (isAnim) {
//
//							isAnim = false
//
//							runOnUiThread {
//
//								if (isAnim) {
//
//									isAnim = false
//									dataSet.get(dataSet.size-1).spannableStringBuilder.clear()
////                            dataSet.removeAt(dataSet.size-1)
//									chatAdapter!!.notifyItemChanged(dataSet.size-1)
//
//								}
//
//
//							}
//
//
//						}
//
//
//					}
//
//				} else if (it.equals("up")){
//
//					status = it
//
//					if (isTurnedOff) {
//
//						if (overlay!=null) {
//
//							if (isWakeUp) {
//
//								runOnUiThread {
//
//									Handler().postDelayed({
//
//										buttonWakeUp!!.visibility = View.VISIBLE
//
//										//startSpeechRecognizer()
//
//									},2000)
//
//
//
//								}
//
//
//							}
//
//							isTurnedOff = false
//
//							overlay!!.hide()
//							overlay = null
//						}
//
//
//					}
//
//
//
//
//
//
//
//
//
////                    if (dataReceiver!=null) {
////
////                        dataReceiver!!.isStopped = false
////                    }
//
//					//isServiceAlert = false
//				}
				//Log.d(TAG, "ChatStatus: "+status)

			}

		}

		override fun onServiceDisconnected(name: ComponentName?) {

			Log.d(SampleActivity::class.java.name, "onServiceDisconnected: ")

			//Log.d(TAG, "onServiceDisconnected: "+isTurnedOff)

			//isBound = false



		}


	}



}