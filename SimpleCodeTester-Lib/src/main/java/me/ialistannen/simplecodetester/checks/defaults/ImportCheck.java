package me.ialistannen.simplecodetester.checks.defaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link Check} verifying all used classes/methods are whitelisted.
 */
public class ImportCheck implements Check {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImportCheck.class);

  private List<Pattern> whitelist;
  private List<Pattern> blacklist;

  /**
   * Creates a new Import check with the given whitelisted patterns
   *
   * @param whitelist the whitelisted patterns
   * @param blacklist the blacklisted patterns, matched <em>after</em> the whitelist
   */
  public ImportCheck(Collection<Pattern> whitelist, Collection<Pattern> blacklist) {
    this.whitelist = new ArrayList<>(whitelist);
    this.blacklist = new ArrayList<>(blacklist);
  }

  @Override
  public CheckResult check(CompiledFile file) {
    visitClassfile(file, new ClassVisitor(Opcodes.ASM7) {
      @Override
      public MethodVisitor visitMethod(int access, String name, String descriptor,
          String signature,
          String[] exceptions) {
        return new FilteringMethodVisitor(file.qualifiedName());
      }
    });
    return CheckResult.emptySuccess(this);
  }

  @Override
  public String toString() {
    return "ImportCheck{" +
        "whitelist=" + whitelist +
        ", blacklist=" + blacklist +
        '}';
  }

  private boolean isWhitelisted(String input) {
    return whitelist.stream()
        .anyMatch(pattern -> pattern.matcher(input).find());
  }

  private boolean isBlacklisted(String input) {
    return blacklist.stream()
        .anyMatch(pattern -> pattern.matcher(input).find());
  }

  private class FilteringMethodVisitor extends MethodVisitor {

    private final String fileFqn;

    private FilteringMethodVisitor(String fileFqn) {
      super(Opcodes.ASM6);
      this.fileFqn = fileFqn;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
        boolean isInterface) {
      checkAccess(owner, name);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
      checkAccess(owner, name);
    }

    private void checkAccess(String owner, String name) {
      // in own class
      if (owner.equals(fileFqn)) {
        return;
      }
      if (!isWhitelisted(sanitizeClassName(owner)) || isBlacklisted(owner)) {
        throw new CheckFailedException("Illegal access to " + owner + "#" + name);
      }
    }

    private String sanitizeClassName(String owner) {
      return owner.replace("/", ".");
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
        Object... bootstrapMethodArguments) {
      LOGGER.warn("Calling dyn " + name + " " + descriptor + " " + bootstrapMethodHandle);
    }
  }

}
