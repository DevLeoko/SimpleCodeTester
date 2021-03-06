package me.ialistannen.simplecodetester.submission;

import java.util.List;
import java.util.Map;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import org.immutables.value.Value;

/**
 * Returns a compiled version of a Submission.
 */
@Value.Immutable
public abstract class CompiledSubmission {

  /**
   * Returns all compiled files in this submission.
   *
   * @return all compiled files in this submission
   */
  public abstract List<CompiledFile> compiledFiles();

  /**
   * Returns all generated auxiliary classes (inner classes, anon, ...)
   *
   * @return all generated auxiliary classes
   */
  public abstract Map<String, byte[]> generatedAuxiliaryClasses();

  /**
   * Returns the compilation output.
   *
   * @return the compilation output
   */
  public abstract CompilationOutput compilationOutput();
}
