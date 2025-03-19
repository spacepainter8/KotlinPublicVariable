import java.io.File
import org.jetbrains.kotlin.*;
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.psi.PsiBlockStatement
import org.jetbrains.kotlin.com.intellij.psi.PsiComment
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.util.EnvironmentUtil
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.fir.scopes.impl.overrides
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.ir.backend.js.ic.KotlinSourceFile
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import kotlin.reflect.KClass


fun isPublic(dcl: PsiElement): Boolean {
    // Checks for public modifier
    if (!dcl.children[0].text.contains(Regex(".*private.*"))
        && !dcl.children[0].text.contains(Regex(".*protected.*"))
        && !dcl.children[0].text.contains(Regex(".*internal.*"))
    ) return true
    else return false
}

fun parseClass(dcl: PsiElement, visitor: KtVisitorVoid){

    var isPublic: Boolean = false
    val children = dcl.allChildren.toList()
    if (dcl.children.size > 0 && dcl.children[0] is KtDeclarationModifierList) {
        isPublic = isPublic(dcl)
    } else isPublic = true

    // If class is public, print it out
    if (isPublic) {
        var hasBody = false
        var i = 0

        // First print out everything but the body, pay attention to primary constructor
        // and its parameters
        while (i < children.size) {
            if (children[i] is KtClassBody) {
                hasBody = true
                break
            } else if (children[i] is KtPrimaryConstructor) {
                // Checking the primary constructor
                var paramListChildIndex = 0
                if (children[i].children[0] !is KtParameterList){
                    // The constructor has declaration modifiers
                    paramListChildIndex = 1
                    var constrIsPublic: Boolean = false
                    if (children[i].children.size >0 && children[i].children[0] is KtDeclarationModifierList){
                        constrIsPublic = isPublic(children[i])
                    } else constrIsPublic = true

                    if (constrIsPublic){
                        print(children[i].children[0].text)
                    } else {
                        i++
                        continue
                    }
                }

                // Continue on to the constructor's parameters
                val paramList: KtParameterList = children[i].children[paramListChildIndex] as KtParameterList
                val params = paramList.allChildren.toList()
                params.forEach {
                    if (it is KtParameter) {
                        var parIsPublic: Boolean = false
                        if (it.children[0] is KtDeclarationModifierList) {
                            parIsPublic = isPublic(it)
                        } else parIsPublic = true

                        // Check whether parameter is actually a property or not
                        if (parIsPublic && it.text.contains(Regex(" ?(val|var) "))) print(it.text)
                    } else if (it !is PsiComment){
                        print(it.text)
                    }
                }
            } else if (children[i] !is PsiComment) print(children[i].text)
            i++
        }

        // Continue on to the body of the class
        if (hasBody) {
            val classBodyChildren = children[children.size - 1].allChildren.toList()
            classBodyChildren.forEach {
                if (it is LeafPsiElement) print(it.text)
                else if (it !is PsiComment) it.accept(visitor)
            }
        }
        println()
    }
}
fun parseFunction(dcl: PsiElement){
    var isPublic: Boolean = false
    if (dcl.children.size > 0 && dcl.children[0] is KtDeclarationModifierList) {
        isPublic = isPublic(dcl)
    } else isPublic = true
    if (isPublic) {
        // If the function is public, print everything but the body of it
        val children = dcl.allChildren.toList()
        for (i in children.indices) {
            if (children[i] !is KtBlockExpression && children[i] !is PsiComment
                && children[i] !is KtCallExpression && children[i].text != "=") print(children[i].text)
            else if (children[i] is KtBlockExpression || children[i].text == "=") break
        }
        println()
    }
}
fun parseProperty(dcl: PsiElement){
    var isPublic: Boolean = false
    if (dcl.children.size > 0 && dcl.children[0] is KtDeclarationModifierList) {
        isPublic = isPublic(dcl)
    } else isPublic = true
    if (isPublic) {
        val children = dcl.allChildren.toList()
        for (i in children.indices) {
            if (children[i] is LeafPsiElement && children[i].text == "=") break
            else if (children[i] !is PsiComment) print(children[i].text)
        }
        println()
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        print("Usage: ./Main.kt <directory>")
        return
    }


    val directoryName = args[0]
    val directory = File(directoryName)
    if (!directory.exists()) {
        print("File/Directory does not exist")
        return
    }



    directory.walk().filter{it.name.endsWith(".kt")}.forEach{

        val file = it
        println("Current file is: " + it.name)
        println()
        println()
        val conf = CompilerConfiguration()
        var environment = KotlinCoreEnvironment.createForProduction(
            Disposable { },
            conf,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        val code = file.readText().replace("\r\n", "\n")


        val psiFile: PsiFile =
            PsiFileFactory.getInstance(environment.project).createFileFromText(file.path, KotlinLanguage.INSTANCE, code)

        val kotFile: KtFile = psiFile as KtFile

        // File visitor
        kotFile.accept(object : KtVisitorVoid() {


            override fun visitKtFile(file: KtFile) {
                file.acceptChildren(this)
            }


            // Visit every element of file, we are only interested in declarations
            override fun visitElement(element: PsiElement) {
                if (element is KtDeclaration) {
                    val dcl = element
                    if (dcl is KtEnumEntry) {
                        print(dcl.text)
                    } else if (dcl is KtClass || dcl is KtObjectDeclaration) {
                        // Declaration is of a class/object/interface
                        parseClass(dcl, this)

                    } else if (dcl is KtNamedFunction) {
                        // Declaration is of a function
                        parseFunction(dcl)
                    } else if (dcl is KtParameter) {
                        // Declaration is of a parameter, but we are only interested in parameters of primary constructor
                        if (dcl.getParent().getParent() is KtPrimaryConstructor)
                            print(dcl.text)
                    } else if (dcl is KtProperty) {
                        // Declaration is of a property
                        parseProperty(dcl)
                    } else dcl.acceptChildren(this)
                } else element.acceptChildren(this)
            }
        })
    }
}