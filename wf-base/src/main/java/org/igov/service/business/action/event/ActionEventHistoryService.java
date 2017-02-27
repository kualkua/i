package org.igov.service.business.action.event;

import java.io.IOException;
import org.springframework.stereotype.Service;
import org.igov.model.action.event.*;
import static org.igov.model.action.event.HistoryEvent_ServiceDaoImpl.DASH;
import java.util.*;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.igov.model.subject.message.SubjectMessage;
import org.igov.service.exception.CRCInvalidException;
import org.igov.service.exception.CommonServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.igov.util.ToolLuna;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Kovilin
 */
@Service
public class ActionEventHistoryService {
    
    private static final Logger LOG = LoggerFactory.getLogger(ActionEventHistoryService.class);
    private final String URI_ADD_HISTORY_EVENT = "/wf/service/action/event/addHistoryEvent_Service";
    
    @Autowired
    private HistoryEventDao historyEventDao;
    
    @Autowired
    private HistoryEvent_ServiceDao historyEventServiceDao;
    
    @Autowired
    private GeneralConfig generalConfig;
    
    @Autowired
    private HttpRequester httpRequester;
    
    private String doRemoteRequest(String sServiceContext, Map<String, String> mParam) throws Exception {
        String soResponse = "";
        if (!generalConfig.getSelfHostCentral().contains("ksds.nads.gov.ua") && !generalConfig.getSelfHostCentral().contains("staff.igov.org.ua")) {
            String sURL = generalConfig.getSelfHostCentral() + sServiceContext;
            LOG.info("(sURL={},mParam={})", sURL, mParam);
            soResponse = httpRequester.getInside(sURL, mParam);
            LOG.info("(soResponse={})", soResponse);
        }
        return soResponse;
    }
    
    public void addHistoryEvent(String sID_Order, String sUserTaskName, Map<String, String> params)
            throws Exception {
        if(sID_Order != null){
            params.put("sID_Order", sID_Order);
            
            if(sUserTaskName != null){
                params.put("sUserTaskName", sUserTaskName);
            }
            
            LOG.info("addHistoryEvent started with params: {}", params);
            int nID_Server = generalConfig.getSelfServerId();
            //int dash_position = sID_Order.indexOf(DASH);
            //int nID_Server = dash_position != -1 ? Integer.parseInt(sID_Order.substring(0, dash_position)) : 0;
            
            LOG.info("nID_Server by DASH: {}", nID_Server);
            LOG.info("nID_Server by sID_order {}", Integer.parseInt(sID_Order.split("-")[0]));
            
            if(nID_Server == Integer.parseInt(sID_Order.split("-")[0])){
                LOG.info("addHistoryEvent make request...");
                doRemoteRequest(URI_ADD_HISTORY_EVENT, params);
            
            }else{
               addActionStatus_Central(
                        sID_Order,
                        Long.parseLong(params.get("nID_Subject")),
                        sUserTaskName,
                        Long.parseLong(params.get("nID_Service")),
                        Long.parseLong(params.get("nID_ServiceData")),
                        Long.parseLong( params.get("nID_Region")),
                        params.get("sID_UA"),
                        params.get("soData"),
                        params.get("sToken"),
                        params.get("sHead"),
                        params.get("sBody"),
                        Long.parseLong(params.get("nID_Proccess_Feedback")),
                        Long.parseLong(params.get("nID_Proccess_Escalation")),
                        Long.parseLong(params.get("nID_StatusType")),
                        1L, 
                        true,
                        true,
                        false
                );
            }
        }
    }
    
    public void setHistoryEvent(HistoryEventType eventType,
            Long nID_Subject, Map<String, String> mParamMessage, Long nID_HistoryEvent_Service, Long nID_Document, String sSubjectInfo) {
        try {
            LOG.info("Method setHistoryEvent started");
            String eventMessage = HistoryEventMessage.createJournalMessage(
                    eventType, mParamMessage);
            LOG.info("Creating journal message ended");
            historyEventDao.setHistoryEvent(nID_Subject, eventType.getnID(),
                    eventMessage, eventMessage, nID_HistoryEvent_Service, nID_Document, sSubjectInfo);
        } catch (IOException e) {
            LOG.error("error: {}, during creating HistoryEvent", e.getMessage());
            LOG.trace("FAIL:", e);
        }
    }
    
    public HistoryEvent_Service getHistoryEventService(String sID_Order) throws CommonServiceException, CRCInvalidException 
    {   
        return historyEventServiceDao.getOrgerByID(sID_Order);
    }

    public HistoryEvent_Service addActionStatus_Central(
            String sID_Order,
            Long nID_Subject,
            String sUserTaskName,
            //String sProcessInstanceName,
            Long nID_Service,
            Long nID_ServiceData,
            Long nID_Region,
            String sID_UA,
            String soData,
            String sToken,
            String sHead,
            String sBody,
            Long nID_Proccess_Feedback,
            Long nID_Proccess_Escalation,
            Long nID_StatusType,
            Long nID_HistoryEventType,
            boolean saveHistoryEventService,
            boolean saveHistoryEvent,
            boolean saveSubjectMessage
            
    ) {
        int dash_position = sID_Order.indexOf(DASH);
        int nID_Server = dash_position != -1 ? Integer.parseInt(sID_Order.substring(0, dash_position)) : 0;
        Long nID_Order = Long.valueOf(sID_Order.substring(dash_position + 1));
        Long nID_Process = ToolLuna.getOriginalNumber(nID_Order);
        
        HistoryEvent_Service oHistoryEvent_Service = null;
        
        if(saveHistoryEventService){
            LOG.info("save HistoryEvent_Service started...");
            oHistoryEvent_Service = new HistoryEvent_Service();
            oHistoryEvent_Service.setnID_Process(nID_Process);
            oHistoryEvent_Service.setsUserTaskName(sUserTaskName);
            oHistoryEvent_Service.setnID_StatusType(nID_StatusType);
            oHistoryEvent_Service.setnID_Subject(nID_Subject);
            oHistoryEvent_Service.setnID_Region(nID_Region);
            oHistoryEvent_Service.setnID_Service(nID_Service);
            oHistoryEvent_Service.setnID_ServiceData(nID_ServiceData);
            oHistoryEvent_Service.setsID_UA(sID_UA);
            oHistoryEvent_Service.setnRate(null);
            oHistoryEvent_Service.setSoData(soData);
            oHistoryEvent_Service.setsToken(sToken);
            oHistoryEvent_Service.setsHead(sHead);
            oHistoryEvent_Service.setsBody(sBody);
            oHistoryEvent_Service.setnID_Server(nID_Server);
            oHistoryEvent_Service.setnID_Proccess_Feedback(nID_Proccess_Feedback);
            oHistoryEvent_Service.setnID_Proccess_Escalation(nID_Proccess_Escalation);
            oHistoryEvent_Service = historyEventServiceDao.addHistoryEvent_Service(oHistoryEvent_Service);
        }
        
        if(saveHistoryEvent){
            LOG.info("save HistoryEvent started...");
            Map<String, String> mParamMessage = new HashMap<>();
            mParamMessage.put(HistoryEventMessage.SERVICE_NAME, sHead);//sProcessInstanceName
            mParamMessage.put(HistoryEventMessage.SERVICE_STATE, sUserTaskName);
            
            if(oHistoryEvent_Service == null)
            {
                try {
                    oHistoryEvent_Service = getHistoryEventService(sID_Order);
                } catch (CRCInvalidException|CommonServiceException e) {
                    LOG.info("can't get HistoryEvent_Service entity: {} ", e);
                }
            }
            
            if(oHistoryEvent_Service != null)
            {
                setHistoryEvent(HistoryEventType.getById(nID_HistoryEventType), nID_Subject, mParamMessage, oHistoryEvent_Service.getId(), null, null);
            }
        }
        
        return oHistoryEvent_Service;
    }
}