import java.io.File
import org.jetbrains.kotlin.*;
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.util.EnvironmentUtil
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.fir.scopes.impl.overrides
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.ir.backend.js.ic.KotlinSourceFile
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClass

//fun what(child:PsiElement){
//    println(child.text + " " + child.javaClass.simpleName)
//    child.getChildren().forEach {child -> what(child)}
//}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ./TryAgain.kt <directory>")
        return
    }


    val fileName = args[0]
    val file = File(fileName)
    if (!file.exists()) {
        println("File does not exist")
        return
    }

    val conf = CompilerConfiguration()



    var environment = KotlinCoreEnvironment.createForProduction(Disposable {  }, CompilerConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES)
    val code = file.readText()

    val psiFile: PsiFile = PsiFileFactory.getInstance(environment.project).createFileFromText(file.path, KotlinLanguage.INSTANCE, code)
    val kotFile: KtFile = psiFile as KtFile

    kotFile.accept(object : KtVisitorVoid() {



        override fun visitKtFile(file: KtFile) {
            file.acceptChildren(this)
        }

        override fun visitKtElement(element: KtElement) {
            println(element.text + "================" + element.javaClass.simpleName);
            if (element is KtDeclaration) println("Decl");
            if (element is KtNamedDeclaration) println("NamedDecl");
            if (element is KtObjectDeclaration) println("ObjectDecl");
            println();println();println();



            element.acceptChildren(this);
        }







    })





    // Read the file line by line
//    val buffer = file.bufferedReader()
//    var line: String? = buffer.readLine()
//    while (line != null) {
//        // Discard the commented out part of the line
//        parseLine(line)
//
//        line = buffer.readLine()
//    }



//    buffer.close()

}