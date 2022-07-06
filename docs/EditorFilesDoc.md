# Editor Files Documentation
This document describes the purposes and usages of the files present in the [Java](../src/main/java/com/rivan/android/studio/visualize/editor) and [Kotlin](../src/main/kotlin/com/rivan/android/studio/visualize/editor) editor directories.

## Descriptions
Descriptions of terms used inside files are provided below.

<details><summary>

### FileEditorProvider</summary>
<ul>
  <li>A FileEditorProvider is a class that provides an editor for all types of files.</li>
  <li>The editor to be created is another class that extends the <strong>FileEditor</strong>.</li>
  <li>A FileEditorProvider overrides atleast 4 methods, i.e.,
        <details id><summary>accept()</summary>
          <ul>
            <li>The <strong>accept()</strong> method is used to determine if the file is accepted by the provider.</li>
            <li>The method is called by the IDE to determine if the provider can handle the file.</li>
            <li>The method returns true if the file is accepted by the provider or false if otherwise.</li>
          </ul>
        </details>
      <details><summary>createEditor()</summary>
          <ul>
            <li>The <strong>createEditor()</strong> method is used to create an editor for the file.</li>
            <li>The method is called by the IDE to create an editor for the file.</li>
            <li>The method returns an instance of the editor to be shown.</li>
          </ul>
        </details>
      <details><summary>getEditorTypeId()</summary>
          <ul>
            <li>The <strong>getEditorTypeId()</strong> method is used to get the type of the editor.</li>
            <li>The method is called by the IDE to get the type of the editor.</li>
            <li>The method returns a string that represents the type of the editor.</li>
          </ul>
        </details>
      <details><summary>getPolicy()</summary>
          <ul>
            <li>The <strong>getPolicy()</strong> method is used to get the policy of the editor.</li>
            <li>The method is called by the IDE to get the policy of the editor.</li>
            <li>The method returns an instance of the policy to be used.</li>
            <li>There are 4 policies available, they are:</li>
            <details><summary>NONE</summary>
              No policies are defined.
            </details>
            <details><summary>PLACE_BEFORE_DEFAULT_EDITOR</summary>
              Place the provided editor before the default editor.
            </details>
            <details><summary>PLACE_AFTER_DEFAULT_EDITOR</summary>
              Place the provided editor after the default editor.
            </details>
            <details><summary>HIDE_DEFAULT_EDITOR</summary>
              Hide the default editor. <strong>The above policies do not hide the default editor.</strong>
            </details>
          </ul>
        </details>
  </li>
</ul>
</details>
