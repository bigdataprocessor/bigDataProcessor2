/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.log.progress;

import java.util.concurrent.atomic.AtomicInteger;

public class LoggingProgressListener implements ProgressListener
{
	private long current;
	private long total;
	private long startTimeMillis;
	private final String msg;

	public LoggingProgressListener( String msg )
	{
		this.msg = msg;
		this.startTimeMillis = System.currentTimeMillis();
		reset();
	}

	@Override
	public void progress( long current, long total )
	{
		this.current = current;
		this.total = total;

		new Thread( () ->
			ProgressHelpers.logProgress(
				total, new AtomicInteger( (int) current ), startTimeMillis, msg )
		).start();
	}

	public boolean isFinished()
	{
		return current >= total;
	}

	public long getCurrent()
	{
		return current;
	}
	
	public long getTotal()
	{
		return total;
	}

	public void reset()
	{
		current = -1;
		total = 0;
	}
}
