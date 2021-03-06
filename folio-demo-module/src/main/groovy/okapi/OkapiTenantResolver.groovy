package okapi

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.multitenancy.TenantResolver
import org.grails.datastore.mapping.multitenancy.exceptions.TenantNotFoundException
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletWebRequest

import javax.servlet.http.HttpServletRequest

/**
 * A tenant resolver that resolves the tenant from the request HTTP Header
 *
 * @author Sergio del Amo
 * @since 6.1.7
 */
@CompileStatic
class OkapiTenantResolver implements TenantResolver {

    public static final String HEADER_NAME = "X-Okapi-Tenant"

    /**
     * The name of the header
     */
    String headerName = HEADER_NAME

    @Override
    Serializable resolveTenantIdentifier() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes()
        if(requestAttributes instanceof ServletWebRequest) {

            HttpServletRequest httpServletRequest = ((ServletWebRequest) requestAttributes).getRequest()
            String tenantId = httpServletRequest.getHeader(headerName.toLowerCase())

            if ( tenantId ) {
                return tenantId+'_grails_demo_module'
            }
            throw new TenantNotFoundException("Tenant could not be resolved from HTTP Header: ${headerName}")
        }

        throw new TenantNotFoundException("Tenant could not be resolved outside a web request")
    }
}
