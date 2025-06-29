import React from 'react'
import { Navigate } from 'react-router';
import { useServerStatus } from '../../context/ServerStatusContext';

type Props = {}

const DashboardPage = (props: Props) => {
    const { setServerStatus, serverStatus } = useServerStatus();

    if (serverStatus !== 'ACTIVATE') {
        return <Navigate to="/verifier-registration" replace />;
    } else {
        return <Navigate to="/verifier-management" replace />;
    }
    
}

export default DashboardPage