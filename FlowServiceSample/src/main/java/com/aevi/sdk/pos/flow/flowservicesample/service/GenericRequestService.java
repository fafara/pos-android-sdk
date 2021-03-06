/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.aevi.sdk.pos.flow.flowservicesample.service;

import android.app.Activity;
import android.util.Log;
import com.aevi.sdk.flow.model.Request;
import com.aevi.sdk.flow.model.Response;
import com.aevi.sdk.flow.service.BaseGenericService;
import com.aevi.sdk.flow.stage.BaseStageModel;
import com.aevi.sdk.flow.stage.GenericStageModel;
import com.aevi.sdk.pos.flow.flowservicesample.ui.LoyaltyBalanceActivity;
import com.aevi.sdk.pos.flow.flowservicesample.ui.ReceiptDeliveryActivity;

import static com.aevi.sdk.flow.constants.FlowServiceEventDataKeys.REJECTED_REASON;
import static com.aevi.sdk.flow.constants.FlowServiceEventTypes.RESPONSE_REJECTED;
import static com.aevi.sdk.flow.constants.FlowServiceEventTypes.RESUME_USER_INTERFACE;
import static com.aevi.sdk.flow.constants.FlowTypes.FLOW_TYPE_RECEIPT_DELIVERY;

/**
 * Illustrates how to implement a service to handle a bespoke request type.
 */
public class GenericRequestService extends BaseGenericService {

    private static final String TAG = GenericRequestService.class.getSimpleName();
    public static final String SHOW_LOYALTY_POINTS_REQUEST = "showLoyaltyPointsBalance";

    @Override
    protected void processRequest(GenericStageModel stageModel) {
        Request request = stageModel.getRequest();
        Log.d(TAG, "Got generic request: " + request.toJson());

        // The requests we handle here require foreground processing (showing UI)
        if (request.shouldProcessInBackground()) {
            stageModel.sendResponse(new Response(request, false, "Can not handle this request in the background"));
            return;
        }

        switch (request.getRequestType()) {
            case SHOW_LOYALTY_POINTS_REQUEST:
                stageModel.processInActivity(getBaseContext(), LoyaltyBalanceActivity.class);
                subscribeToFlowServiceEvents(stageModel, LoyaltyBalanceActivity.class);
                break;
            case FLOW_TYPE_RECEIPT_DELIVERY:
                stageModel.processInActivity(getBaseContext(), ReceiptDeliveryActivity.class);
                subscribeToFlowServiceEvents(stageModel, ReceiptDeliveryActivity.class);
                break;
            default:
                stageModel.sendResponse(new Response(request, false, "Unsupported request type"));
                break;

        }
    }

    protected void subscribeToFlowServiceEvents(BaseStageModel model, Class<? extends Activity> activityToRestart) {
        model.getEvents().subscribe(event -> {
            switch (event.getType()) {
                case RESUME_USER_INTERFACE:
                    // In this sample, we simply restart the activity as it contains no state
                    model.processInActivity(getBaseContext(), activityToRestart);
                    break;
                case RESPONSE_REJECTED:
                    String rejectReason = event.getData().getStringValue(REJECTED_REASON);
                    Log.w(TAG, "Response rejected: " + rejectReason);
                    break;
                default:
                    Log.i(TAG, "Received flow service event: " + event.getType());
                    break;
            }
        });
    }
}
