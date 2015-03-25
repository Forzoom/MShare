/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mshare.server.rtsp;

public class RtspCmdMap {
	protected Class<? extends RtspCmd> cmdClass;
	String name;


	public RtspCmdMap(String name, Class<? extends RtspCmd> cmdClass) {
		super();
		this.name = name;
		this.cmdClass = cmdClass;
	}

	public Class<? extends RtspCmd> getCommand() {
		return cmdClass;
	}

	public void setCommand(Class<? extends RtspCmd> cmdClass) {
		this.cmdClass = cmdClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
