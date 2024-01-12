package net.java.games.input;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.java.games.input.DefaultControllerEnvironment;

class PluginClassLoader extends ClassLoader {
   private static String pluginDirectory;
   private static final FileFilter JAR_FILTER = new PluginClassLoader.JarFileFilter();
   // $FF: synthetic field
   static final boolean $assertionsDisabled;

   public PluginClassLoader() {
      super(Thread.currentThread().getContextClassLoader());
   }

   protected Class findClass(String name) throws ClassNotFoundException {
      byte[] b = this.loadClassData(name);
      return this.defineClass(name, b, 0, b.length);
   }

   private byte[] loadClassData(String name) throws ClassNotFoundException {
      if(pluginDirectory == null) {
         pluginDirectory = DefaultControllerEnvironment.libPath + File.separator + "controller";
      }

      try {
         return this.loadClassFromDirectory(name);
      } catch (Exception var5) {
         try {
            return this.loadClassFromJAR(name);
         } catch (IOException var4) {
            throw new ClassNotFoundException(name, var4);
         }
      }
   }

   private byte[] loadClassFromDirectory(String name) throws ClassNotFoundException, IOException {
      StringTokenizer tokenizer = new StringTokenizer(name, ".");
      StringBuffer path = new StringBuffer(pluginDirectory);

      while(tokenizer.hasMoreTokens()) {
         path.append(File.separator);
         path.append(tokenizer.nextToken());
      }

      path.append(".class");
      File file = new File(path.toString());
      if(!file.exists()) {
         throw new ClassNotFoundException(name);
      } else {
         FileInputStream fileInputStream = new FileInputStream(file);
         if(!$assertionsDisabled && file.length() > 2147483647L) {
            throw new AssertionError();
         } else {
            int length = (int)file.length();
            byte[] bytes = new byte[length];
            int length2 = fileInputStream.read(bytes);
            if(!$assertionsDisabled && length != length2) {
               throw new AssertionError();
            } else {
               return bytes;
            }
         }
      }
   }

   private byte[] loadClassFromJAR(String name) throws ClassNotFoundException, IOException {
      File dir = new File(pluginDirectory);
      File[] jarFiles = dir.listFiles(JAR_FILTER);
      if(jarFiles == null) {
         throw new ClassNotFoundException("Could not find class " + name);
      } else {
         for(int i = 0; i < jarFiles.length; ++i) {
            JarFile jarfile = new JarFile(jarFiles[i]);
            JarEntry jarentry = jarfile.getJarEntry(name + ".class");
            if(jarentry != null) {
               InputStream jarInputStream = jarfile.getInputStream(jarentry);
               if(!$assertionsDisabled && jarentry.getSize() > 2147483647L) {
                  throw new AssertionError();
               }

               int length = (int)jarentry.getSize();
               if(!$assertionsDisabled && length < 0) {
                  throw new AssertionError();
               }

               byte[] bytes = new byte[length];
               int length2 = jarInputStream.read(bytes);
               if(!$assertionsDisabled && length != length2) {
                  throw new AssertionError();
               }

               return bytes;
            }
         }

         throw new FileNotFoundException(name);
      }
   }

   static {
      $assertionsDisabled = !PluginClassLoader.class.desiredAssertionStatus();
   }

   private static class JarFileFilter implements FileFilter {
      private JarFileFilter() {
      }

      public boolean accept(File file) {
         return file.getName().toUpperCase().endsWith(".JAR");
      }
   }
}
