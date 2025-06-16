
public class RTException extends RuntimeException 
{
   public String msg;
   
   RTException( String msg )
   {
      super();
      this.msg = msg;
   }
}
