package com.sequenceiq.it.cloudbreak.dto.mock;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.emptyRunningParameter;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.it.TestParameter;
import com.sequenceiq.it.cloudbreak.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.ResourcePropertyProvider;
import com.sequenceiq.it.cloudbreak.assertion.Assertion;
import com.sequenceiq.it.cloudbreak.cloud.v4.CloudProvider;
import com.sequenceiq.it.cloudbreak.context.RunningParameter;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.context.TestErrorLog;
import com.sequenceiq.it.cloudbreak.dto.CloudbreakTestDto;
import com.sequenceiq.it.cloudbreak.dto.mock.answer.RequestData;
import com.sequenceiq.it.cloudbreak.mock.ExecuteQueryToMockInfrastructure;

@Prototype
public class HttpMock implements CloudbreakTestDto {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMock.class);

    @Inject
    private TestParameter testParameter;

    @Inject
    @Qualifier("cloudProviderProxy")
    private CloudProvider cloudProvider;

    @Inject
    private ResourcePropertyProvider resourcePropertyProvider;

    @Value("${mock.infrastructure.host:localhost}")
    private String mockInfrastructureHost;

    @Inject
    private ExecuteQueryToMockInfrastructure executeQueryToMockInfrastructure;

    private String name;

    private String resourceNameType;

    private TestContext testContext;

    private List<RequestData> requestList = new LinkedList<>();

    protected HttpMock(TestContext testContext) {
        this.testContext = testContext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceNameType() {
        return resourceNameType;
    }

    public TestParameter getTestParameter() {
        return testParameter;
    }

    protected CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    @Override
    public String getLastKnownFlowChainId() {
        return null;
    }

    @Override
    public String getLastKnownFlowId() {
        return null;
    }

    @Override
    public CloudbreakTestDto valid() {
        return this;
    }

    public <O extends CloudbreakTestDto> O given(String key, Class<O> clss) {
        return testContext.given(key, clss);
    }

    public <O extends CloudbreakTestDto> O given(Class<O> clss) {
        return testContext.given(clss);
    }

    public <O extends CloudbreakTestDto> O init(Class<O> clss) {
        return testContext.init(clss);
    }

    public ResourcePropertyProvider getResourcePropertyProvider() {
        return resourcePropertyProvider;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name: " + getName() + "]";
    }

    public <T> T whenRequested(Class<T> endpoint) {
        return (T) Proxy.newProxyInstance(
                HttpMock.class.getClassLoader(),
                new Class[]{endpoint},
                (proxy, method, args) -> {
                    Method httpMethod = Method.build(method.getName());
                    SparkUriParameters parameters = new SparkUriAnnotationHandler(endpoint, method).getParameters();
                    return method.getReturnType().getConstructor(Method.class, String.class, Class.class, HttpMock.class, ExecuteQueryToMockInfrastructure.class)
                            .newInstance(httpMethod, parameters.getUri(), parameters.getType(), this, executeQueryToMockInfrastructure);

                });
    }

    public <T> String getUrl(Class<T> endpoint, java.lang.reflect.Method method) {
        SparkUriParameters parameters = new SparkUriAnnotationHandler(endpoint, method).getParameters();
        return "https://" + mockInfrastructureHost + ":10090" + parameters.getUri();
    }

    public List<RequestData> getRequestList() {
        return requestList;
    }

    public <T> T then(Class<T> endpoint) {
        return whenRequested(endpoint);
    }

    public HttpMock then(Assertion<HttpMock, CloudbreakClient> assertion) {
        return then(assertion, emptyRunningParameter());
    }

    public HttpMock then(Assertion<HttpMock, CloudbreakClient> assertion, RunningParameter runningParameter) {
        return getTestContext().then((HttpMock) this, CloudbreakClient.class, assertion, runningParameter);
    }

    public HttpMock then(List<Assertion<HttpMock, CloudbreakClient>> assertions) {
        List<RunningParameter> runningParameters = new ArrayList<>(assertions.size());
        for (int i = 0; i < assertions.size(); i++) {
            runningParameters.add(emptyRunningParameter());
        }
        return then(assertions, runningParameters);
    }

    public HttpMock then(List<Assertion<HttpMock, CloudbreakClient>> assertions, List<RunningParameter> runningParameters) {
        for (int i = 0; i < assertions.size() - 1; i++) {
            getTestContext().then(this, CloudbreakClient.class, assertions.get(i), runningParameters.get(i));
        }
        return getTestContext().then(this, CloudbreakClient.class, assertions.get(assertions.size() - 1),
                runningParameters.get(runningParameters.size() - 1));
    }

    public void validate() {
        testContext.handleExceptionsDuringTest(TestErrorLog.FAIL);
    }

    @Override
    public CloudPlatform getCloudPlatform() {
        return CloudPlatform.MOCK;
    }
}
