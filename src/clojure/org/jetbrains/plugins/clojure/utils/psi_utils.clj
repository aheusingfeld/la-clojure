(ns org.jetbrains.plugins.clojure.utils.psi-utils
  (:import [com.intellij.psi PsiFile PsiElement PsiReference]
           [org.jetbrains.plugins.clojure.psi.util ClojurePsiFactory]
           [org.jetbrains.plugins.clojure.parser ClojureElementTypes ClojurePsiCreator]
           [com.intellij.openapi.editor Editor]
           [org.jetbrains.plugins.clojure.file ClojureFileType]
           [com.intellij.lang ASTNode]
           [com.intellij.psi.tree IElementType]
           [org.jetbrains.plugins.clojure.psi.api ClojureFile]
           [org.jetbrains.plugins.clojure.psi.api.defs ClDef]))

(defmacro maybe
  [f]
  `(fn [arg#]
     (if-let [x# arg#]
       (~f x#))))

(defn n-iter
  [x n f]
  (nth
    (iterate
      (maybe f)
      x)
    n))

(defn find-element-by-offset
  [^PsiFile file offset]
  (.findElementAt
    file
    offset))

(defn find-ref-by-offset
  [^PsiFile file offset]
  (.findReferenceAt
    file
    offset))

(defn get-current-position
  [^Editor editor]
  (some-> editor
    .getCaretModel
    .getOffset))

(defn get-caret-psi-element
  [editor file]
  (find-element-by-offset
    file
    (get-current-position editor)))


(defn get-caret-psi-ref
  [editor file]
  (find-ref-by-offset
    file
    (get-current-position editor)))

(defn is-clojure?
  [^PsiFile file]
  (let [lang (.getLanguage file)]
    (=
      lang
      (ClojureFileType/CLOJURE_LANGUAGE))))

(defn instance-of?
  [^PsiElement psi ^IElementType expected]
  (=
    (some-> psi
      .getNode
      .getElementType)
    expected))



(defn is-quoted?
  [^PsiElement psi]
  (instance-of?
    psi
    (ClojureElementTypes/QUOTED_FORM)))

(defn is-symbol?
  [^PsiElement psi]
  (instance-of?
    psi
    (ClojureElementTypes/SYMBOL)))

(defn is-def?
  [psi]
  (instance-of?
    psi
    (ClojureElementTypes/DEF)))

(defn create-psi
  [^ASTNode node]
  (ClojurePsiCreator/createElement node))


(defn ^PsiElement lift
  [^PsiElement psi by-n]
  (n-iter
    psi
    by-n
    (fn [^PsiElement psi] (.getParent psi))))

(defn ^PsiElement right-down
  [^PsiElement psi by-n]
  (n-iter
    psi
    by-n
    (fn [^PsiElement psi] (last (.getChildren psi)))))

(defn ^PsiElement left-down
  [^PsiElement psi by-n]
  (n-iter
    psi
    by-n
    (fn [^PsiElement psi] (first (.getChildren psi)))))

(defn create-symbol-node-from-text
  [project text]
  (.createSymbolNodeFromText
    (ClojurePsiFactory/getInstance project)
    text))

(defn get-namespace
  [^ClojureFile file]
  (.getNamespace file))

(defn check-def
  [^ClDef def check-name check-ns]
  (and
    (=
      (.getDefinedName def)
      check-name)
    (=
      (some-> def
        .getContainingFile
        get-namespace)
      check-ns)))
