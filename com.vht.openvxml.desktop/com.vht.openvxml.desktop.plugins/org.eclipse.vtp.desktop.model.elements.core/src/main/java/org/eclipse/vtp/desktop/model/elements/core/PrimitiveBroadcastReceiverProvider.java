package org.eclipse.vtp.desktop.model.elements.core;

import java.util.List;

import com.openmethods.openvxml.desktop.model.workflow.design.IExitBroadcastReceiver;

public interface PrimitiveBroadcastReceiverProvider {
	public List<IExitBroadcastReceiver> getExitBroadcastReceivers();

	public void setExitBroadcastReceivers(List<IExitBroadcastReceiver> receivers);
}
