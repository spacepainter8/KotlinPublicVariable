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



fun main(args: Array<String>) {
    if (args.isEmpty()) {
        print("Usage: ./TryAgain.kt <directory>")
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

        val fileHash = file.readBytes().contentHashCode()
        println("File hash: $fileHash")

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




        kotFile.accept(object : KtVisitorVoid() {


            override fun visitKtFile(file: KtFile) {
                file.acceptChildren(this)
            }

//

            override fun visitElement(element: PsiElement) {
                if (element is KtDeclaration) {
                    val dcl = element
                    if (dcl is KtEnumEntry) {
                        print(dcl.text)
                    } else if (dcl is KtClass || dcl is KtObjectDeclaration) {

                        var isPublic: Boolean = false
                        val children = dcl.allChildren.toList()
                        if (dcl.children.size > 0 && dcl.children[0] is KtDeclarationModifierList) {
                            if (!dcl.children[0].text.contains(Regex(".*private.*"))
                                && !dcl.children[0].text.contains(Regex(".*protected.*"))
                                && !dcl.children[0].text.contains(Regex(".*internal.*"))
                            ) isPublic = true
                        } else isPublic = true

                        if (isPublic) {
                            var hasBody = false
                            var i = 0

                            while (i < children.size) {
                                if (children[i] is KtClassBody) {
                                    hasBody = true
                                    break
                                } else if (children[i] is KtPrimaryConstructor) {
                                    var paramListChildIndex = 0
                                    if (children[i].children[0] !is KtParameterList){
                                        paramListChildIndex = 1
                                        var constrIsPublic: Boolean = false
                                        if (!children[i].children[0].text.contains(Regex(".*private.*"))
                                            && !children[i].children[0].text.contains(Regex(".*protected.*"))
                                            && !children[i].children[0].text.contains(Regex(".*internal.*"))
                                        ) constrIsPublic = true
                                        if (constrIsPublic){
                                            print(children[i].children[0].text)
                                        } else {
                                            i++
                                            continue
                                        }
                                    }
                                    val paramList: KtParameterList = children[i].children[paramListChildIndex] as KtParameterList
                                    val params = paramList.allChildren.toList()
                                    params.forEach {
                                        if (it is KtParameter) {
                                            var parIsPublic: Boolean = false
                                            if (it.children[0] is KtDeclarationModifierList) {
                                                if (!it.children[0].text.contains(Regex(".*private.*"))
                                                    && !it.children[0].text.contains(Regex(".*protected.*"))
                                                    && !it.children[0].text.contains(Regex(".*internal.*"))
                                                ) parIsPublic = true
                                            } else parIsPublic = true

                                            if (parIsPublic) print(it.text)
                                        } else if (it !is PsiComment){
                                            print(it.text)
                                        }
                                    }


                                } else if (children[i] !is PsiComment) print(children[i].text)
                                i++
                            }


                            if (hasBody) {
                                val classBodyChildren = children[children.size - 1].allChildren.toList()
                                classBodyChildren.forEach {
                                    if (it is LeafPsiElement) print(it.text)
                                    else if (it !is PsiComment) it.accept(this)
                                }
                            }

                            println()


                        }


                    } else if (dcl is KtNamedFunction) {
                        var isPublic: Boolean = false
                        if (dcl.children.size > 0 && dcl.children[0] is KtDeclarationModifierList) {
                            if (!dcl.children[0].text.contains(Regex(".*private.*"))
                                && !dcl.children[0].text.contains(Regex(".*protected.*"))
                                && !dcl.children[0].text.contains(Regex(".*internal.*"))
                            ) isPublic = true
                        } else isPublic = true
                        if (isPublic) {
                            val children = dcl.allChildren.toList()
                            for (i in children.indices) {
                                if (children[i] !is KtBlockExpression && children[i] !is PsiComment
                                    && children[i] !is KtCallExpression && children[i].text != "=") print(children[i].text)
                                else if (children[i] is KtBlockExpression || children[i].text == "=") break
                            }
                            println()
                        }
                    } else if (dcl is KtParameter) {
                        if (dcl.getParent().getParent() is KtPrimaryConstructor)
                            print(dcl.text)
                    } else if (dcl is KtProperty) {
                        var isPublic: Boolean = false
                        if (dcl.children.size > 0 && dcl.children[0] is KtDeclarationModifierList) {
                            if (!dcl.children[0].text.contains(Regex(".*private.*"))
                                && !dcl.children[0].text.contains(Regex(".*protected.*"))
                                && !dcl.children[0].text.contains(Regex(".*internal.*"))
                            ) isPublic = true
                        } else isPublic = true
                        if (isPublic) {
                            val children = dcl.allChildren.toList()
                            for (i in children.indices) {
                                if (children[i] !is PsiComment) print(children[i].text)
                            }
                            println()
                        }
                    } else dcl.acceptChildren(this)
                } else element.acceptChildren(this)


            }


        })

    }




}