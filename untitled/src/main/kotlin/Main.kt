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


    val fileName = args[0]
    val file = File(fileName)
    if (!file.exists()) {
        print("File does not exist")
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

//        override fun visitKtElement(element: KtElement) {
//            if (element is KtDeclaration){
//                val dcl : KtDeclaration = element
//                if (dcl is KtClass || dcl is KtObjectDeclaration) {
//                    var isPublic:Boolean = false
//                    if ((dcl.firstChild is KtDeclarationModifierList && dcl.firstChild.text=="public") || dcl.firstChild !is KtDeclarationModifierList)
//                        isPublic = true
//                    if (isPublic) {
//                        var hasBody = false
//                        var i = 0
//                        while (i < dcl.children.size) {
//                            if (dcl.children[i] is KtClassBody){
//                                hasBody = true
//                                break
//                            } else if (dcl.children[i] is KtParameter){
//                                var parIsPublic:Boolean = false
//                                if (dcl.children[i].firstChild != null && dcl.children[i].firstChild is KtDeclarationModifierList
//                                    && dcl.children[i].firstChild.text == "public") {
//                                    parIsPublic = true
//                                }
//                                if (parIsPublic) print(dcl.children[i].text)
//                            } else print(dcl.children[i].text)
//                            i++
//                        }
//
//                        if (hasBody) print("{")
//
//                        while(i<dcl.children.size) {
//                            dcl.children[i].accept(this)
//                        }
//
//                        if (hasBody) {
//                            for (j in dcl.children.size-1 downTo 0) {
//                                if (dcl.children[i] is LeafPsiElement
//                                    && dcl.children[i].text=="}") {
//                                    print(dcl.children[i].text)
//                                    break
//                                }
//                            }
//                        }
//
//                    }
//
//
//
//                } else if (dcl is KtNamedFunction) {
//                    var isPublic:Boolean = false
//                    if (dcl.firstChild != null && dcl.firstChild is KtDeclarationModifierList &&
//                        dcl.firstChild.text == "public") isPublic = true
//                    if (isPublic) print(dcl.text)
//                } else if (dcl is KtParameter){
//                    if (dcl.getParent().getParent() is KtPrimaryConstructor)
//                        print(dcl.text)
//                } else dcl.acceptChildren(this)
//            } else element.acceptChildren(this)
//        }

//        override fun visitDeclaration(dcl: KtDeclaration) {
//            if (dcl is KtClass || dcl is KtObjectDeclaration) {
//                var isPublic:Boolean = false
//                if (dcl.firstChild != null && dcl.firstChild is KtDeclarationModifierList &&
//                    dcl.firstChild.text == "public") isPublic = true
//
//                if (isPublic) {
//                    var hasBody = false
//                    var i = 0
//                    while (i < dcl.children.size) {
//                        if (dcl.children[i] is KtClassBody){
//                            hasBody = true
//                            break
//                        } else if (dcl.children[i] is KtParameter){
//                            var parIsPublic:Boolean = false
//                            if (dcl.children[i].firstChild != null && dcl.children[i].firstChild is KtDeclarationModifierList
//                                && dcl.children[i].firstChild.text == "public") {
//                                parIsPublic = true
//                            }
//                            if (parIsPublic) print(dcl.children[i])
//                        } else print(dcl.children[i])
//                        i++
//                    }
//
//                    if (hasBody) print("{")
//
//                    while(i<dcl.children.size) {
//                        dcl.children[i].accept(this)
//                    }
//
//                    if (hasBody) {
//                        for (j in dcl.children.size-1 downTo 0) {
//                            if (dcl.children[i] is LeafPsiElement
//                                && dcl.children[i].text=="}") {
//                                print(dcl.children[i].text)
//                                break
//                            }
//                        }
//                    }
//
//                }
//
//
//
//            } else if (dcl is KtNamedFunction) {
//                var isPublic:Boolean = false
//                if (dcl.firstChild != null && dcl.firstChild is KtDeclarationModifierList &&
//                    dcl.firstChild.text == "public") isPublic = true
//                if (isPublic) print(dcl.text)
//            } else if (dcl is KtParameter){
//                if (dcl.getParent().getParent() is KtPrimaryConstructor)
//                        print(dcl.text)
//            } else dcl.acceptChildren(this);
//        }

//        override fun visitKtElement(element: KtElement) {
//            if (element is KtDeclaration){
//                val dcl : KtDeclaration = element
//                if (dcl is KtClass || dcl is KtObjectDeclaration) {
//                    var isPublic:Boolean = false
//                    if ((dcl.firstChild is KtDeclarationModifierList && dcl.firstChild.text=="public") || dcl.firstChild !is KtDeclarationModifierList)
//                        isPublic = true
//                    if (isPublic) {
//                        var hasBody = false
//                        var i = 0
//                        while (i < dcl.children.size) {
//                            if (dcl.children[i] is KtClassBody){
//                                hasBody = true
//                                break
//                            } else if (dcl.children[i] is KtParameter){
//                                var parIsPublic:Boolean = false
//                                if (dcl.children[i].firstChild != null && dcl.children[i].firstChild is KtDeclarationModifierList
//                                    && dcl.children[i].firstChild.text == "public") {
//                                    parIsPublic = true
//                                }
//                                if (parIsPublic) print(dcl.children[i].text)
//                            } else print(dcl.children[i].text)
//                            i++
//                        }
//
//                        if (hasBody) print("{")
//
//                        while(i<dcl.children.size) {
//                            dcl.children[i].accept(this)
//                        }
//
//                        if (hasBody) {
//                            for (j in dcl.children.size-1 downTo 0) {
//                                if (dcl.children[i] is LeafPsiElement
//                                    && dcl.children[i].text=="}") {
//                                    print(dcl.children[i].text)
//                                    break
//                                }
//                            }
//                        }
//
//                    }
//
//
//
//                } else if (dcl is KtNamedFunction) {
//                    var isPublic:Boolean = false
//                    if (dcl.firstChild != null && dcl.firstChild is KtDeclarationModifierList &&
//                        dcl.firstChild.text == "public") isPublic = true
//                    if (isPublic) print(dcl.text)
//                } else if (dcl is KtParameter){
//                    if (dcl.getParent().getParent() is KtPrimaryConstructor)
//                        print(dcl.text)
//                } else dcl.acceptChildren(this)
//            } else element.acceptChildren(this)
//        }


        override fun visitElement(element: PsiElement) {
            if (element is KtDeclaration){
                val dcl = element
                if (dcl is KtEnumEntry){
                  print(dcl.text)
                } else if (dcl is KtClass || dcl is KtObjectDeclaration) {

                    var isPublic:Boolean = false
                    val children = dcl.allChildren.toList()
                    if (dcl.children[0] is KtDeclarationModifierList){
                        if (!dcl.children[0].text.contains(Regex(".*private.*"))
                            && !dcl.children[0].text.contains(Regex(".*protected.*"))
                            && !dcl.children[0].text.contains(Regex(".*internal.*"))) isPublic = true
                    } else isPublic = true

                    if (isPublic) {
                        var hasBody = false
                        var i = 0

                        while (i < children.size) {
                            if (children[i] is KtClassBody){
                                hasBody = true
                                break
                            } else if (children[i] is KtPrimaryConstructor){
                                val paramList: KtParameterList = children[i].children[0] as KtParameterList
                                val params = paramList.allChildren.toList()
                                params.forEach {
                                    if (it is KtParameter){
                                        var parIsPublic:Boolean = false
                                        if (it.children[0] is KtDeclarationModifierList){
                                            if (!it.children[0].text.contains(Regex(".*private.*"))
                                                && !it.children[0].text.contains(Regex(".*protected.*"))
                                                && !it.children[0].text.contains(Regex(".*internal.*"))) parIsPublic = true
                                        } else parIsPublic = true

                                        if (parIsPublic) print(it.text)
                                    } else {
                                        print(it.text)
                                    }
                                }


                            } else if (children[i] !is PsiComment) print(children[i].text)
                            i++
                        }


                        if (hasBody){
                            val classBodyChildren = children[children.size-1].allChildren.toList()
                            classBodyChildren.forEach {
                                if (it is LeafPsiElement) print(it.text)
                                else it.accept(this)
                            }
                        }

                        println()






                    }



                } else if (dcl is KtNamedFunction) {
                    var isPublic:Boolean = false
                    if (dcl.children[0] is KtDeclarationModifierList){
                        if (!dcl.children[0].text.contains(Regex(".*private.*"))
                            && !dcl.children[0].text.contains(Regex(".*protected.*"))
                            && !dcl.children[0].text.contains(Regex(".*internal.*"))) isPublic = true
                    } else isPublic = true
                    if (isPublic) {
                        val children = dcl.allChildren.toList()
                        for(i in children.indices){
                            if (children[i] !is KtBlockExpression && children[i] !is PsiComment) print(children[i].text)
                            else if (children[i] is KtBlockExpression) break
                        }
                        println()
                    }
                } else if (dcl is KtParameter){
                    if (dcl.getParent().getParent() is KtPrimaryConstructor)
                        print(dcl.text)
                } else if (dcl is KtProperty){
                    var isPublic:Boolean = false
                    if (dcl.children[0] is KtDeclarationModifierList){
                        if (!dcl.children[0].text.contains(Regex(".*private.*"))
                            && !dcl.children[0].text.contains(Regex(".*protected.*"))
                            && !dcl.children[0].text.contains(Regex(".*internal.*"))) isPublic = true
                    } else isPublic = true
                    if (isPublic) {
                        val children = dcl.allChildren.toList()
                        for(i in children.indices){
                            if (children[i] !is PsiComment) print(children[i].text)
                        }
                        println()
                    }
                }
                else dcl.acceptChildren(this)
            } else element.acceptChildren(this)





        }






    })



}