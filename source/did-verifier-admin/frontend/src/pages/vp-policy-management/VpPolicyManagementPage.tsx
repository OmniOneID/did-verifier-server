import { useEffect } from 'react';
import { useNavigate } from 'react-router';

type Props = {}

const VpPolicyManagementPage = (props: Props) => {
    const navigate = useNavigate();

    useEffect(() => {
      navigate('vp-policy-management/service-management');
    }, [navigate]);
  
    return null;
}

export default VpPolicyManagementPage