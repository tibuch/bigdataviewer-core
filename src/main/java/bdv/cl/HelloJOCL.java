package bdv.cl;

import static com.jogamp.opencl.CLMemory.Mem.READ_ONLY;
import static com.jogamp.opencl.CLMemory.Mem.WRITE_ONLY;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.lang.System.out;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;

/**
 * Hello Java OpenCL example. Adds all elements of buffer A to buffer B and
 * stores the result in buffer C.<br/>
 * Sample was inspired by the Nvidia VectorAdd example written in C/C++ which is
 * bundled in the Nvidia OpenCL SDK.
 *
 * @author Michael Bien
 */
public class HelloJOCL
{

	public static void main( final String[] args ) throws IOException
	{

		// set up (uses default CLPlatform and creates context for all devices)
		final CLContext context = CLContext.create();
		out.println( "created " + context );
		out.println();

		// always make sure to release the context under all circumstances
		// not needed for this particular sample but recommented
		try
		{
			// select fastest device
			for ( final CLDevice dev : context.getDevices() )
			{
				out.println( "available " + dev );
				out.println( "dev.getMaxClockFrequency() = \"" + dev.getName() + "\"");
				out.println( "dev.getMaxClockFrequency() = " + dev.getMaxClockFrequency() );
				out.println( "dev.getMaxComputeUnits() = " + dev.getMaxComputeUnits() );
				out.println( "dev.getMaxWorkGroupSize() = " + dev.getMaxWorkGroupSize() );
				out.println( "dev.getDriverVersion() = " + dev.getDriverVersion() );
				out.println( "dev.isGLMemorySharingSupported() = " + dev.isGLMemorySharingSupported() );
				out.println( "dev.getGlobalMemSize() = " + dev.getGlobalMemSize() );
				out.println( "dev.getGlobalMemCacheSize() = " + dev.getGlobalMemCacheSize() );
				out.println( "dev.getGlobalMemCachelineSize() = " + dev.getGlobalMemCachelineSize() );
				out.println( "dev.isLittleEndian() = " + dev.isLittleEndian() );
				out.println( "dev.isMemoryUnified() = " + dev.isMemoryUnified() );
				out.println( "dev.isImageSupportAvailable() = " + dev.isImageSupportAvailable() );
				out.println( "dev.getMaxImage3dWidth() = " + dev.getMaxImage3dWidth() );
				out.println( "dev.getMaxImage3dHeight() = " + dev.getMaxImage3dHeight() );
				out.println( "dev.getMaxImage3dDepth() = " + dev.getMaxImage3dDepth() );
				out.println();
			}

//			final CLDevice device = context.getMaxFlopsDevice( Type.GPU );
//			final CLDevice device = context.getMaxFlopsDevice();
			final CLDevice device = context.getDevices()[ 0 ];
			out.println( "using " + device );

			// create command queue on device.
			final CLCommandQueue queue = device.createCommandQueue();

			final int elementCount = 1444477; // Length of arrays to process
			final int localWorkSize = min( device.getMaxWorkGroupSize(), 256 );
			// Local work size dimensions
			final int globalWorkSize = roundUp( localWorkSize, elementCount );
			// rounded up to the nearest multiple of the localWorkSize

			// load sources, create and build program
			final CLProgram program = context.createProgram( HelloJOCL.class.getResourceAsStream( "../../VectorAdd.cl" ) ).build();

			// A, B are input buffers, C is for the result
			final CLBuffer< FloatBuffer > clBufferA = context.createFloatBuffer( globalWorkSize, READ_ONLY );
			final CLBuffer< FloatBuffer > clBufferB = context.createFloatBuffer( globalWorkSize, READ_ONLY );
			final CLBuffer< FloatBuffer > clBufferC = context.createFloatBuffer( globalWorkSize, WRITE_ONLY );

			out.println( "used device memory: " + ( clBufferA.getCLSize() + clBufferB.getCLSize() + clBufferC.getCLSize() ) / 1000000 + "MB" );

			// fill input buffers with random numbers
			// (just to have test data; seed is fixed -> results will not change
			// between runs).
			fillBuffer( clBufferA.getBuffer(), 12345 );
			fillBuffer( clBufferB.getBuffer(), 67890 );

			// get a reference to the kernel function with the name 'VectorAdd'
			// and map the buffers to its input parameters.
			final CLKernel kernel = program.createCLKernel( "VectorAdd" );
			kernel.putArgs( clBufferA, clBufferB, clBufferC ).putArg( elementCount );

			// asynchronous write of data to GPU device,
			// followed by blocking read to get the computed results back.
			long time = nanoTime();
			queue.putWriteBuffer( clBufferA, false ).putWriteBuffer( clBufferB, false ).put1DRangeKernel( kernel, 0, globalWorkSize, localWorkSize ).putReadBuffer( clBufferC, true );
			time = nanoTime() - time;

			// print first few elements of the resulting buffer to the console.
			out.println( "a+b=c results snapshot: " );
			for ( int i = 0; i < 10; i++ )
				out.print( clBufferC.getBuffer().get() + ", " );
			out.println( "...; " + clBufferC.getBuffer().remaining() + " more" );

			out.println( "computation took: " + ( time / 1000000 ) + "ms" );

		}
		finally
		{
			// cleanup all resources associated with this context.
			context.release();
		}

	}

	private static void fillBuffer( final FloatBuffer buffer, final int seed )
	{
		final Random rnd = new Random( seed );
		while ( buffer.remaining() != 0 )
			buffer.put( rnd.nextFloat() * 100 );
		buffer.rewind();
	}

	private static int roundUp( final int groupSize, final int globalSize )
	{
		final int r = globalSize % groupSize;
		if ( r == 0 )
		{
			return globalSize;
		}
		else
		{
			return globalSize + groupSize - r;
		}
	}

}